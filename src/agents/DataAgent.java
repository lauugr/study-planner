package agents;

import jade.core.Agent;

public class DataAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("DataAgent iniciado: " + getLocalName());
    }
}