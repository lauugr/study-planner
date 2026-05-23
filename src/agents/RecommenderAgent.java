package agents;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import utils.DFUtils;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

// Agente encargado de procesar la informacion recibida y generar la recomendacion del plan de estudio
public class RecommenderAgent extends Agent {
    @Override
    protected void setup() {
        // Se muestra por consola que el agente se ha iniciado correctamente
        System.out.println("RecommenderAgent iniciado: " + getLocalName());

        // Se registra en el Directory Facilitator el servicio de recomendacion ofrecido
        // por este agente
        DFUtils.registerService(
                this,
                "recommendation-service",
                "Servicio de recomendación de planes de estudio");

        // Se anade un comportamiento ciclico para atender solicitudes de recomendacion
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Se define una plantilla para recibir solo mensajes REQUEST de recomendacion
                MessageTemplate template = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchConversationId("study-plan-recommendation"));

                // Se intenta recibir un mensaje que cumpla la plantilla
                ACLMessage request = receive(template);

                if (request != null) {
                    System.out.println("Solicitud recibida de " + request.getSender().getLocalName()
                            + ": " + request.getContent());

                    // Se solicitan al DataAgent los datos necesarios para realizar la recomendacion
                    String dataRules = requestDataRules();

                    System.out.println("Datos recibidos del DataAgent: " + dataRules);

                    // Se procesa la informacion recibida y se obtiene el plan recomendado mediante
                    // Weka
                    String recommendation = recommendStudyPlan(request.getContent(), dataRules);

                    // Se crea la respuesta a partir del mensaje recibido
                    ACLMessage response = request.createReply();
                    response.setPerformative(ACLMessage.INFORM);
                    response.setContent(recommendation);

                    // Se envia la recomendacion al agente que hizo la solicitud
                    send(response);
                    System.out.println("Respuesta enviada: " + recommendation);
                } else {
                    // Si no hay mensajes disponibles, el comportamiento se bloquea temporalmente
                    block();
                }
            }
        });
    }

    // Solicita al DataAgent los datos necesarios para realizar la recomendacion
    private String requestDataRules() {
        // Se busca en el Directory Facilitator un agente que ofrezca el servicio de
        // datos
        AID[] dataAgents = DFUtils.searchService(this, "data-service");

        if (dataAgents.length == 0) {
            return "sin-datos";
        }

        // Se crea un mensaje ACL de tipo REQUEST para solicitar los datos
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(dataAgents[0]);
        request.setConversationId("study-plan-data");
        request.setContent("solicitud=reglas");

        // Se envia la solicitud al DataAgent
        send(request);
        System.out.println("Solicitud de datos enviada al DataAgent");

        // Se crea una plantilla para esperar la respuesta del DataAgent
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("study-plan-data"));

        // Se espera de forma bloqueante un maximo de 10 segundos a recibir la respuesta
        // del DataAgent
        ACLMessage response = blockingReceive(template, 10_000);

        if (response != null) {
            return response.getContent();
        }

        System.out.println("Tiempo de espera agotado, no se ha recibido respuesta del DataAgent a tiempo.");
        return "sin-datos";
    }

    // Calcula una recomendacion aplicando clasificacion supervisada con Weka
    private String recommendStudyPlan(String data, String dataRules) {
        // Si el DataAgent no proporciona datos, se informa de que no puede calcularse
        // la recomendacion
        if (dataRules.equals("sin-datos") || dataRules.contains("reglas=error")) {
            return "No se ha podido generar una recomendación porque no se han recibido datos válidos.";
        }

        String datasetPath = getValueFromExternalData(dataRules, "dataset");

        if (datasetPath.equals("")) {
            return "No se ha podido generar una recomendación porque no se ha encontrado el dataset";
        }

        try {
            // Se clasifica la situacion del usuario mediante el algoritmo J48 de Weka
            String plan = classifyStudyPlan(data, datasetPath);

            // Se obtiene la distribucion asociada al plan recomendado desde las reglas
            // externas
            String distribucion = getDistributionForPlan(dataRules, plan);

            return "Plan recomendado: " + plan
                    + "\nTécnica aplicada: clasificación supervisada con Weka (J48)"
                    + "\nDistribución sugerida: " + distribucion;

        } catch (Exception e) {
            e.printStackTrace();
            return "No se ha podido generar una recomendación por un error durante la clasificación";
        }
    }

    // Clasifica los datos introducidos por el usuario usando un modelo J48 de Weka
    private String classifyStudyPlan(String data, String datasetPath) throws Exception {
        // Se carga el dataset externo proporcionado por el DataAgent
        DataSource source = new DataSource(datasetPath);
        Instances dataset = source.getDataSet();

        // Se indica que la clase a predecir es el ultimo atributo: plan
        if (dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
        }

        // Se crea y entrena el clasificador J48 con los ejemplos del dataset
        Classifier classifier = new J48();
        classifier.buildClassifier(dataset);

        // Se crea una nueva instancia con los datos introducidos por el usuario
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);

        instance.setValue(dataset.attribute("dias"), getIntValue(data, "dias"));
        instance.setValue(dataset.attribute("horas"), getIntValue(data, "horas"));
        instance.setValue(dataset.attribute("nivel"), getStringValue(data, "nivel"));
        instance.setValue(dataset.attribute("dificultad"), getStringValue(data, "dificultad"));
        instance.setValue(dataset.attribute("temario"), getIntValue(data, "temario"));

        // La clase plan queda sin asignar porque es lo que se quiere predecir
        instance.setMissing(dataset.classAttribute());

        // Se clasifica la instancia y se obtiene el nombre del plan predicho
        double prediction = classifier.classifyInstance(instance);

        return dataset.classAttribute().value((int) prediction);
    }

    // Extrae un valor de los datos externos proporcionados por el DataAgent
    private String getValueFromExternalData(String dataRules, String key) {
        String[] lines = dataRules.split("\\n");

        for (String line : lines) {
            String[] pair = line.split("=", 2);

            if (pair.length == 2 && pair[0].trim().equals(key)) {
                return pair[1].trim();
            }
        }

        return "";
    }

    // Busca en las reglas proporcionadas por el DataAgent la distribucion asociada
    // a un plan
    private String getDistributionForPlan(String dataRules, String plan) {
        String[] lines = dataRules.split("\\n");

        for (String line : lines) {
            String[] pair = line.split("=", 2);

            if (pair.length == 2 && pair[0].trim().equals(plan)) {
                return pair[1].trim();
            }
        }

        return "No se ha encontrado una distribución específica para este plan";
    }

    // Extrae un valor entero de una cadena con formato clave=valor
    private int getIntValue(String data, String key) {
        try {
            return Integer.parseInt(getStringValue(data, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Extrae un valor textual de una cadena con formato clave=valor
    private String getStringValue(String data, String key) {
        String[] fields = data.split(";");

        for (String field : fields) {
            String[] pair = field.split("=");

            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }

        return "";
    }
}