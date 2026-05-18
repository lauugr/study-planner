package agents;

import jade.core.Agent;
import utils.DFUtils;

// Agente encargado de gestionar los datos necesarios para realizar la recomendacion
public class DataAgent extends Agent {
    @Override
    protected void setup() {
        // Se muestra por consola que el agente se ha iniciado correctamente
        System.out.println("DataAgent iniciado: " + getLocalName());

        // Se registra en el Directory Facilitator el servicio de datos ofrecido por este agente
        DFUtils.registerService(
            this,
            "data-service",
            "Servicio de datos para planes de estudio"
        );
    }
}