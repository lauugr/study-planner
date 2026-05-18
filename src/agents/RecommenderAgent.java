package agents;

import jade.core.Agent;

public class RecommenderAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("RecommenderAgent iniciado: " + getLocalName());
    }
}