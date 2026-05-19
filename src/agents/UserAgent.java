package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.DFUtils;

// Agente encargado de representar al usuario y solicitar la recomendacion del plan de estudio
public class UserAgent extends Agent {
    @Override
    protected void setup() {
        // Se muestra por consola que el agente se ha iniciado correctamente
        System.out.println("UserAgent iniciado: " + getLocalName());

        // Se anade un comportamiento que espera unos segundos antes de buscar el servicio, para dar tiempo a que el resto de agentes se registren en el DF
        addBehaviour(new WakerBehaviour(this, 2000) {
            @Override
            protected void onWake() {
                // Se busca en el Directory Facilitator un agente que ofrezca el servicio de recomendacion
                AID[] recommenders = DFUtils.searchService(myAgent, "recommendation-service");

                // Si se encuentra algun agente recomendador, se muestra su nombre por consola
                if (recommenders.length > 0) {
                    AID recommender = recommenders[0];

                    System.out.println("Servicio recommendation-service encontrado en: "
                            + recommender.getLocalName());

                    // Se crea un mensaje ACL de tipo REQUEST para solicitar una recomendacion
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.addReceiver(recommender);
                    request.setConversationId("study-plan-recommendation");
                    request.setContent("dias=7;horas=2;nivel=medio;dificultad=alta;temario=40");

                    // Se envia la solicitud al agente recomendador
                    send(request);
                    System.out.println("Solicitud enviada al agente recomendador");

                    // Se crea una plantilla para esperar unicamente la respuesta a esta conservacion
                    MessageTemplate template = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchConversationId("study-plan-recommendation")
                    );

                    // Se espera de forma bloqueante a recibir la respuesta del recomendador
                    ACLMessage response = blockingReceive(template);

                    // Se muestra la recomendacion recibida
                    if (response != null) {
                        System.out.println("Respuesta recibida del recomendador: " + response.getContent());
                    }
                } else {
                    // Si no se encuentra ningun agente con ese servicio, se informa por consola
                    System.out.println("No se ha encontrado ningún servicio recommendation-service");
                }
            }
        });
    }
}