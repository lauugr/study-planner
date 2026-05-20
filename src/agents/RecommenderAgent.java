package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.DFUtils;

// Agente encargado de procesar la informacion recibida y generar la recomendacion del plan de estudio
public class RecommenderAgent extends Agent {
    @Override
    protected void setup() {
        // Se muestra por consola que el agente se ha iniciado correctamente
        System.out.println("RecommenderAgent iniciado: " + getLocalName());

        // Se registra en el Directory Facilitator el servicio de recomendacion ofrecido por este agente
        DFUtils.registerService(
            this,
            "recommendation-service",
            "Servicio de recomendación de planes de estudio"
        );

        // Se anade un comportamiento ciclico para atender solicitudes de recomendacion
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Se define una plantilla para recibir solo mensajes REQUEST de recomendacion
                MessageTemplate template = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchConversationId("study-plan-recommendation")
                );

                // Se intenta recibir un mensaje que cumpla la plantilla
                ACLMessage request = receive(template);

                if (request != null) {
                    System.out.println("Solicitud recibida de " + request.getSender().getLocalName()
                            + ": " + request.getContent());

                    // Se solicitan al DataAgent los datos o reglas necesarios para realizar la recomendacion
                    String dataRules = requestDataRules();

                    System.out.println("Datos recibidos del DataAgent: " + dataRules);

                    // Se procesa la informacion recibida y se obtiene el plan recomendado
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
        // Se busca en el Directory Facilitator un agente que ofrezca el servicio de datos
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
            MessageTemplate.MatchConversationId("study-plan-data")
        );

        // Se espera de forma bloqueante a recibir la respuesta del DataAgent
        ACLMessage response = blockingReceive(template);

        if (response != null) {
            return response.getContent();
        }

        return "sin-datos";
    }

    // Calcula una recomendacion sencilla a partir de los datos recibidos
    private String recommendStudyPlan(String data, String dataRules) {
        int dias = getIntValue(data, "dias");
        int horas = getIntValue(data, "horas");
        int temario = getIntValue(data, "temario");

        String nivel = getStringValue(data, "nivel");
        String dificultad = getStringValue(data, "dificultad");

        // Si el DataAgent no proporciona datos, se informa de que no puede calcularse la recomendacion
        if (dataRules.equals("sin-datos") || dataRules.contains("reglas=error")) {
            return "No se ha podido generar una recomendación porque no se han recibido datos válidos";
        }

        int horasTotales = dias * horas;
        int temarioRestante = 100 - temario;

        String plan;
        String motivo;

        // Esta casi listo si tiene mucho conocimiento y ademas tiene buen nivel
        if (temario >= 80 && nivel.equals("alto")) {
            plan = "mantenimiento";
            motivo = "el nivel actual es alto y el porcentaje de temario preparado es elevado.";
        }
        // Es urgente si:
        // 1. Queda menos de una semana y le falta mas del 30% del temario
        // 2. Tiene muy pocas horas de estudio y le falta mas de la mitad del temario
        else if ((dias <= 7 && temarioRestante > 30) || (horasTotales < 15 && temarioRestante > 50)) {
            plan = "intensivo";
            motivo = "queda poco tiempo disponible o el porcentaje de temario pendiente es alto.";
        }
        // Necesita reforzar conocimientos si tiene el nivel muy bajo o percibe la asignatura muy dificil
        else if (nivel.equals("bajo") || dificultad.equals("alta")) {
            plan = "refuerzo";
            motivo = "el nivel actual o la dificultad percibida de la asignatura requieren reforzar los contenidos.";
        }
        // El resto de casos un plan equilibrado
        else {
            plan = "equilibrado";
            motivo = "hay margen suficiente para combinar teoría, práctica y repaso.";
        }

        String distribucion = getDistributionForPlan(dataRules, plan);

        return "Plan recomendado: " + plan
                + "\nMotivo: " + motivo
                + "\nDistribución sugerida: " + distribucion;
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

    // Busca en las reglas proporcionadas por el DataAgent la distribucion asociada a un plan
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
}