package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
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
                    System.out.println("Servicio recommendation-service encontrado en: "
                            + recommenders[0].getLocalName());
                } else {
                    // Si no se encuentra ningun agente con ese servicio, se informa por consola
                    System.out.println("No se ha encontrado ningún servicio recommendation-service");
                }
            }
        });
    }
}