package agents;

import jade.core.Agent;

import java.util.Scanner;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import utils.DFUtils;

// Agente encargado de representar al usuario y solicitar la recomendacion del plan de estudio
public class UserAgent extends Agent {
    @Override
    protected void setup() {
        // Se muestra por consola que el agente se ha iniciado correctamente
        System.out.println("UserAgent iniciado: " + getLocalName());

        // Se anade un comportamiento simple para gestionar el menu de consultas del usuario
        addBehaviour(new SimpleBehaviour(this) {
            private boolean finalizado = false;
            private boolean primera = true;

            private Scanner sc = new Scanner(System.in);
            
            @Override
            public void action() {
                // la primera vez se espera unos segundos antes de buscar el servicio, para dar tiempo a que el resto de agentes se registren en el DF
                if (primera){
                    doWait(2000);
                    primera = false;
                }

                // Se busca en el Directory Facilitator un agente que ofrezca el servicio de recomendacion
                AID[] recommenders = DFUtils.searchService(myAgent, "recommendation-service");

                // Si se encuentra algun agente recomendador, se muestra su nombre por consola
                if (recommenders.length > 0) {
                    AID recommender = recommenders[0];

                    System.out.println("Servicio recommendation-service encontrado en: "
                            + recommender.getLocalName());

                    // Se interacciona con el usuario
                    System.out.println("\n------------------------------");
                    System.out.println("        MENU PRINCIPAL        ");
                    System.out.println("------------------------------");

                    System.out.println("¿Generar plan de estudios? (s/n): ");
                    String inicio = sc.next().toLowerCase();

                    // Se comprueba la respuesta del usuario
                    if (inicio.equals("n") || inicio.equals("no")){
                        System.out.println("Finalizando servicio. Suerte con el estudio :)");
                        finalizado = true;
                        return;
                    } else if (!inicio.equals("s") && !inicio.equals("si") && !inicio.equals("sí")){
                        System.out.println("Opcion no valida, introduce 's'(sí) o 'n'(no)");
                        return;
                    }
  
                    System.out.println("Bienvenido al Planificador de estudio");

                    System.out.println("\n------------------------------");
                    System.out.println("         NUEVA CONSULTA       ");
                    System.out.println("------------------------------");


                    System.out.print("¿Cuántos días faltan para el examen?: ");
                    int dias = sc.nextInt();

                    System.out.print("¿Cuántas horas al dia puedes estudiar?: ");
                    int horas = sc.nextInt();

                    System.out.print("¿Cuál es tu nivel actual? (bajo/medio/alto): ");
                    String nivel = sc.next().toLowerCase();

                    System.out.print("¿Cuál es la dificultad de la asignatura? (baja/media/alta): ");
                    String dificultad = sc.next().toLowerCase();

                    System.out.print("¿Qué porcentaje de temario llevas preparado? (0-100): ");
                    int temario = sc.nextInt();

                    String contenidoMsg = "dias=" + dias + ";horas=" + horas + ";nivel=" + nivel 
                                            + ";dificultad=" + dificultad + ";temario=" + temario;

                    // Se crea un mensaje ACL de tipo REQUEST para solicitar una recomendacion
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.addReceiver(recommender);
                    request.setConversationId("study-plan-recommendation");
                    request.setContent(contenidoMsg);

                    // Se envia la solicitud al agente recomendador
                    send(request);
                    System.out.println("Solicitud enviada al agente recomendador");

                    // Se crea una plantilla para esperar unicamente la respuesta a esta conversacion
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
                    System.out.println("Buscando servicio recommendation-service");
                    doWait(1000);
                }
            }

            @Override
            public boolean done(){
                if(finalizado) sc.close();
                return finalizado;
            }
        });
    }
}