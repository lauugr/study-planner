package agents;

import javax.swing.SwingUtilities;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import utils.DFUtils;

// Agente encargado de representar al usuario y solicitar la recomendacion del plan de estudio
public class UserAgent extends Agent {

    // Interfaz de usuario
    private UserAgentInterfaz interfaz;

    @Override
    protected void setup() {
        // Se muestra por consola que el agente se ha iniciado correctamente
        System.out.println("UserAgent iniciado: " + getLocalName());

        SwingUtilities.invokeLater(() -> {
            interfaz = new UserAgentInterfaz(this);
            interfaz.setVisible(true);
        });
    }

    public void solicitarRecomendacion(String contenidoMsg) {
        addBehaviour(new SimpleBehaviour(this) {
            @Override
            public void action() {
                interfaz.mostrarEstado("Buscando servicio recommendation-service...");

                // Se busca en el Directory Facilitator un agente que ofrezca el servicio de
                // recomendacion
                AID[] recommenders = DFUtils.searchService(myAgent, "recommendation-service");

                // Si se encuentra algun agente recomendador, se muestra su nombre por consola
                if (recommenders.length > 0) {
                    AID recommender = recommenders[0];
                    interfaz.mostrarEstado("Servicio recommendation-service encontrado, enviando solicitud...");
                    System.out.println("Servicio recommendation-service encontrado en: "
                            + recommender.getLocalName());

                    // Se crea un mensaje ACL de tipo REQUEST para solicitar una recomendacion
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.addReceiver(recommender);
                    request.setConversationId("study-plan-recommendation");
                    request.setContent(contenidoMsg);

                    // Se envia la solicitud al agente recomendador
                    send(request);
                    System.out.println("Solicitud enviada al agente recomendador");

                    // Se crea una plantilla para esperar unicamente la respuesta a esta
                    // conversacion
                    MessageTemplate template = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchConversationId("study-plan-recommendation"));

                    // Se espera de forma bloqueante un maximo de 15 segundos a recibir la respuesta
                    // del recomendador
                    ACLMessage response = blockingReceive(template, 15_000);

                    // Se muestra la recomendacion recibida
                    if (response != null) {
                        interfaz.mostrarEstado("Análisis completado.");
                        interfaz.mostrarRespuesta(response.getContent());
                        System.out.println("Respuesta recibida del recomendador: " + response.getContent());
                    } else {
                        interfaz.mostrarEstado("Error: Tiempo de espera agotado.");
                        interfaz.mostrarRespuesta(
                                "Tiempo de espera agotado, no se ha recibido respuesta del RecommenderAgent a tiempo.");
                    }
                } else {
                    interfaz.mostrarEstado("Error: Servicio no encontrado.");
                    interfaz.mostrarRespuesta(
                            "No se ha encontrado ningún servicio recommendation-service. Inténtelo de nuevo más tarde.");
                }
            }

            @Override
            public boolean done() {
                return true;
            }
        });
    }
}