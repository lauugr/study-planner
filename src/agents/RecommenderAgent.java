package agents;

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

                    // Se procesa la informacion recibida y se obtiene el plan recomendado
                    String recommendation = recommendStudyPlan(request.getContent());

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

    // Calcula una recomendacion sencilla a partir de los datos recibidos
    private String recommendStudyPlan(String data) {
        int dias = getIntValue(data, "dias");
        int horas = getIntValue(data, "horas");
        int temario = getIntValue(data, "temario");

        String nivel = getStringValue(data, "nivel");
        String dificultad = getStringValue(data, "dificultad");

        // Regla 1: si quedan pocos dias o queda mucho temario, se recomienda un plan intensivo
        if (dias <= 7 || temario < 40) {
            return "Plan recomendado: intensivo";
        }

        // Regla 2: si el nivel es bajo o la dificultad es alta, se recomienda un plan de refuerzo
        if (nivel.equals("bajo") || dificultad.equals("alta")) {
            return "Plan recomendado: de refuerzo";
        }

        // Regla 3: si el usuario tiene buen nivel y bastante temario preparado, se recomienda mantenimiento
        if (nivel.equals("alto") && temario >= 75) {
            return "Plan recomendado: de mantenimiento";
        }

        // Regla 4: en el resto de casos, se recomienda un plan equilibrado
        if (horas >= 2) {
            return "Plan recomendado: equilibrado";
        }

        return "Plan recomendado: de refuerzo";
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