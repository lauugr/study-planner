package agents;

import jade.core.Agent;
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
    }
}