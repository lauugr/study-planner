package agents;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.DFUtils;

// Agente encargado de gestionar los datos necesarios para realizar la recomendacion
public class DataAgent extends Agent {

    private final String RUTA = "data/reglas_de_estudio.txt";

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

        // Se anade un comportamiento ciclico para atender solicitudes de datos
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Se define una plantilla para recibir solo mensajes REQUEST relacionados con datos
                MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("study-plan-data")
                );

                // Se intenta recibir un mensaje que cumpla la plantilla
                ACLMessage request = receive(template);

                if (request != null) {
                    System.out.println("Solicitud de datos recibida de "
                        + request.getSender().getLocalName());

                    // De momento se devuelven reglas basicas como texto
                    String rules = leerReglas(RUTA);

                    // Se crea la respuesta al mensaje recibido
                    ACLMessage response = request.createReply();
                    response.setPerformative(ACLMessage.INFORM);
                    response.setContent(rules);

                    // Se envia la informacion al agente solicitante
                    send(response);
                    System.out.println("Datos enviados al agente recomendador");
                } else {
                    // Si no hay mensajes disponibles, el comportamiento se bloquea temporalmente
                    block();
                }
            }
        });
    }

    // Metodo para leer documento con reglas
    private String leerReglas(String ruta){
        StringBuilder reglas = new StringBuilder();
        File archivo = new File(ruta);

        try (Scanner lector = new Scanner(archivo)){
            // Se anade un salto de linea para mantener separadas las reglas leidas del fichero
            while (lector.hasNextLine()) {
                reglas.append(lector.nextLine()).append("\n");
            }
            return reglas.toString();
        } catch (FileNotFoundException e){
            System.err.println("Archivo no encontrado en la ruta: " + ruta);
            return "reglas=error;planes=ninguno";
        }
    }
}