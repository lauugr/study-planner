package agents;

import jade.core.Agent;

public class UserAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("UserAgent iniciado: " + getLocalName());
    }
}