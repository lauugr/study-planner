package agents;

import java.util.Scanner;

import jade.core.*;
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
                // La primera vez se espera unos segundos antes de buscar el servicio, para dar tiempo a que el resto de agentes se registren en el DF
                if (primera) {
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

                    boolean generarPlan = pedirSiNo(sc, "¿Generar plan de estudios? (s/n): ");

                    if (!generarPlan) {
                        System.out.println("Finalizando servicio. Suerte con el estudio :)");
                        finalizado = true;
                        return;
                    }
  
                    System.out.println("Bienvenido al Planificador de estudio");

                    System.out.println("\n------------------------------");
                    System.out.println("         NUEVA CONSULTA       ");
                    System.out.println("------------------------------");

                    int dias = pedirEntero(sc, "¿Cuántos días faltan para el examen?: ", 1, 365);
                    int horas = pedirEntero(sc, "¿Cuántas horas al dia puedes estudiar?: ", 1, 24);
                    String nivel = pedirOpcion(sc, "¿Cuál es tu nivel actual? (bajo/medio/alto): ",
                            new String[]{"bajo", "medio", "alto"});
                    String dificultad = pedirOpcion(sc, "¿Cuál es la dificultad de la asignatura? (baja/media/alta): ",
                            new String[]{"baja", "media", "alta"});
                    int temario = pedirEntero(sc, "¿Qué porcentaje de temario llevas preparado? (0-100): ", 0, 100);

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

                    // Se espera de forma bloqueante un maximo de 15 segundos a recibir la respuesta del recomendador
                    ACLMessage response = blockingReceive(template, 15_000);

                    // Se muestra la recomendacion recibida
                    if (response != null) {
                        System.out.println("Respuesta recibida del recomendador: " + response.getContent());
                    } else {
                        System.out.println("Tiempo de espera agotado, no se ha recivido respuesta del RecommenderAgent a tiempo.");
                    }
                } else {
                    // Si no se encuentra ningun agente con ese servicio, se informa por consola
                    System.out.println("No se ha encontrado ningún servicio recommendation-service");
                    System.out.println("Buscando servicio recommendation-service");
                    doWait(1000);
                }
            }

            @Override
            public boolean done() {
                if (finalizado) sc.close();
                return finalizado;
            }
        });
    }

    // Solicita al usuario una respuesta afirmativa o negativa y valida la entrada
    private boolean pedirSiNo(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String respuesta = sc.nextLine().trim().toLowerCase();

            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
                return true;
            }

            if (respuesta.equals("n") || respuesta.equals("no")) {
                return false;
            }

            System.out.println("Opcion no valida. Introduce 's' para si o 'n' para no.");
        }
    }

    // Solicita un numero entero dentro de un rango determinado
    private int pedirEntero(Scanner sc, String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String entrada = sc.nextLine().trim();

            try {
                int valor = Integer.parseInt(entrada);

                if (valor >= min && valor <= max) {
                    return valor;
                }

                System.out.println("Valor no valido. Introduce un numero entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Entrada no valida. Introduce un numero entero.");
            }
        }
    }

    // Solicita una opcion textual y comprueba que pertenezca al conjunto de opciones permitidas
    private String pedirOpcion(Scanner sc, String mensaje, String[] opcionesValidas) {
        while (true) {
            System.out.print(mensaje);
            String entrada = sc.nextLine().trim().toLowerCase();

            for (String opcion : opcionesValidas) {
                if (entrada.equals(opcion)) {
                    return entrada;
                }
            }

            System.out.println("Opcion no valida. Introduce una de las opciones indicadas.");
        }
    }
}