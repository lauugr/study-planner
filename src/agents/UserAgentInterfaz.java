package agents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Clase encargada de la interfaz de usuario
public class UserAgentInterfaz extends JFrame {
    
    //Agente encargado
    private UserAgent miAgente;

    //Componetes en la interfaz
    private JSpinner panelDias;
    private JSpinner panelHoras;
    private JComboBox<String> panelNivel;
    private JComboBox<String> panelDificultad;
    private JSpinner panelEstudiado;
    private JTextArea txtResultado;
    private JLabel estadoActual;


    

    public UserAgentInterfaz (UserAgent agente){
        //super("Interfaz de usuario - " + agente.getLocalName());
        this.miAgente = agente;

        //Ventana emergente
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        //Si se cierra la venta el agente se suspende
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                miAgente.doSuspend();
            }
        });

        //Entrada de datos para el agente
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        panel.add(new JLabel("Días hasta el examen:"));
        panelDias = new JSpinner(new SpinnerNumberModel(15, 1, 365, 1));
        panel.add(panelDias);

        panel.add(new JLabel("Horas diarias de estudio:"));
        panelHoras = new JSpinner(new SpinnerNumberModel(2, 1, 24, 1));
        panel.add(panelHoras);

        panel.add(new JLabel("Nivel actual:"));
        panelNivel = new JComboBox<>(new String[]{"bajo", "medio", "alto"});
        panelNivel.setSelectedItem("medio");
        panel.add(panelNivel);

        panel.add(new JLabel("Dificultad de la asignatura:"));
        panelDificultad = new JComboBox<>(new String[]{"baja", "media", "alta"});
        panelDificultad.setSelectedItem("media");
        panel.add(panelDificultad);

        panel.add(new JLabel("Porcentaje de temario preparado:"));
        panelEstudiado = new JSpinner(new SpinnerNumberModel(50, 0, 100, 1));
        panel.add(panelEstudiado);

        add(panel, BorderLayout.NORTH);

        //Boton para ejecucion
        JPanel panelBotones = new JPanel();
        JButton btnGenerar = new JButton("Generar Plan de Estudio");
        //Modificar tamaño Boton¿?
        btnGenerar.addActionListener(e -> {
            txtResultado.setText("");
            mostarEstado("Procesando datos");

            //Extraer datos
            int dias = (int) panelDias.getValue();
            int horas = (int) panelHoras.getValue();
            String nivel = (String) panelNivel.getSelectedItem();
            String dificultad = (String) panelDificultad.getSelectedItem();
            int temario = (int) panelEstudiado.getValue();

            //Contruir mensaje y enviar

            String contenidoMsg = "dias=" + dias + ";horas=" + horas + ";nivel=" + nivel 
                                            + ";dificultad=" + dificultad + ";temario=" + temario;
            miAgente.solicitarRecomendacion(contenidoMsg);
        });

    panelBotones.add(btnGenerar);
    add(panelBotones, BorderLayout.CENTER);

    //Panel de resultados
    JPanel panelResultados = new JPanel(new BorderLayout(0, 5));
    panelResultados.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

    estadoActual = new JLabel("Estado: Esperando...");
    panelResultados.add(estadoActual, BorderLayout.NORTH);

    txtResultado = new JTextArea(7, 30);
    txtResultado.setEditable(false);
    
    JScrollPane paneltxt = new JScrollPane(txtResultado);
    panelResultados.add(paneltxt, BorderLayout.CENTER);

    add(panelResultados, BorderLayout.SOUTH);
    }

    public void mostarEstado(String estado) {
        SwingUtilities.invokeLater(() -> { estadoActual.setText("Estado: " + estado); });
    }

    public void mostrarRespuesta(String content) {
        SwingUtilities.invokeLater(() -> { txtResultado.setText(content); });
    }
}
