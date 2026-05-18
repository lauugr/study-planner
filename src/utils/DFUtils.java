package utils;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class DFUtils {

    // Registra en el Directory Facilitator (DF) un servicio ofrecido por un agente
    public static void registerService(Agent agent, String serviceType, String serviceName) {
        // Se crea la descripcion del agente que se va a registrar en el DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        // Se crea la descripcion del servicio ofrecido por el agente
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);

        // Se asocia el servicio a la descripcion del agente
        dfd.addServices(sd);

        try {
            // Se registra el agente y su servicio en el DF
            DFService.register(agent, dfd);
            System.out.println(agent.getLocalName() + " ha registrado el servicio: " + serviceType);
        } catch (FIPAException e) {
            // Se captura cualquier error producido durante el registro en el DF
            System.err.println("Error registrando el servicio " + serviceType + " en " + agent.getLocalName());
            e.printStackTrace();
        }
    }

    // Busca en el Directory Facilitator (DF) agentes que ofrezcan un tipo de servicio concreto
    public static AID[] searchService(Agent agent, String serviceType) {
        // Se crea una plantilla de busqueda para indicar el tipo de servicio buscado
        DFAgentDescription template = new DFAgentDescription();

        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);

        // Se anade el servicio buscado a la plantilla
        template.addServices(sd);

        try {
            // Se realiza la busqueda en el DF usando la plantilla definida
            DFAgentDescription[] results = DFService.search(agent, template);

            // Se extraen los identificadores de los agentes encontrados
            AID[] agents = new AID[results.length];
            for (int i = 0; i < results.length; i++) {
                agents[i] = results[i].getName();
            }

            return agents;
        } catch (FIPAException e) {
            // Si se produce un error durante la busqueda, se devuelve una lista vacia
            System.err.println("Error buscando el servicio " + serviceType + " desde " + agent.getLocalName());
            e.printStackTrace();
            return new AID[0];
        }
    }
}
