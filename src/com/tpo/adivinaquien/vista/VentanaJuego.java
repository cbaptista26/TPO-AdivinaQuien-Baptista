package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.app.SimulacionEstrategias;
import com.tpo.adivinaquien.app.VerificacionCatalogo;
import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ===========================================================================
 * LA VISTA SWING
 * ===========================================================================
 *
 * Igual que JuegoConsola, esta clase es UNICAMENTE presentacion: dibuja
 * componentes y escucha clics. No decide nada del juego. Cada vez que el
 * usuario hace algo, se lo pasa a ControladorPartida, que llama al motor.
 *
 * Los 23 personajes y los 6 botones de pregunta NO estan en el formulario:
 * se generan aca por codigo dentro de panelTablero y panelFiltros, que en el
 * .form quedaron vacios a proposito. Asi, si maniana se agrega un filtro o un
 * personaje, no hay que redibujar nada en el disenador.
 */
public class VentanaJuego implements ControladorPartida.Observador {

    // -----------------------------------------------------------------
    // CAMPOS DEL FORMULARIO (los genera el GUI Designer, no los toques)
    // -----------------------------------------------------------------
    private JPanel panelPrincipal;
    private JLabel lblTurno;
    private JLabel lblCandidatos;
    private JButton btnNuevaPartida;
    private JPanel panelTablero;
    private JPanel panelFiltros;
    private JButton btnArriesgar;
    private JButton btnSugerencia;
    private JTextArea txtRazonamiento;

    // -----------------------------------------------------------------
    // ESTADO DE LA VISTA
    // -----------------------------------------------------------------

    private RegistroSwing registro;
    private ControladorPartida controlador;

    /** Selector con los cuatro modos, equivalente al menu de la consola. */
    private JComboBox<String> comboModo;

    /** Reproduce el modo maquina vs maquina turno a turno. */
    private Timer temporizador;

    /** Una tarjeta por personaje, para poder tacharla cuando se descarta. */
    private final Map<Integer, JLabel> tarjetas = new HashMap<>();

    // -----------------------------------------------------------------
    // ARRANQUE
    // -----------------------------------------------------------------

    /**
     * Crea los componentes que declara el formulario.
     *
     * El diseno visual se hizo con el Swing UI Designer y quedo guardado en
     * VentanaJuego.form. La instanciacion se hace aca por codigo y no con el
     * metodo que genera el disenador, porque ese metodo depende de forms_rt.jar
     * (una libreria interna de IntelliJ) y el proyecto no compilaria fuera del
     * IDE. No se pierde nada: el layout definitivo lo arma reorganizarLayout(),
     * cuya primera instruccion es panelPrincipal.removeAll().
     */
    private void crearComponentes() {
        panelPrincipal  = new JPanel();
        lblTurno        = new JLabel("Turno");
        lblCandidatos   = new JLabel("Candidatos: 23");
        btnNuevaPartida = new JButton("Iniciar");
        panelTablero    = new JPanel();
        panelFiltros    = new JPanel();
        btnArriesgar    = new JButton("Arriesgar");
        btnSugerencia   = new JButton("\u00bfQue preguntaria la maquina?");
        txtRazonamiento = new JTextArea();

        comboModo = new JComboBox<>(new String[]{
                "1 - Jugador vs Maquina",
                "2 - Maquina vs Maquina",
                "3 - Simulacion de las 23 partidas",
                "4 - Verificar catalogo e insercion binaria"
        });
    }

    public VentanaJuego() {
        crearComponentes();
        registro = new RegistroSwing(txtRazonamiento);
        controlador = new ControladorPartida(registro, this);

        txtRazonamiento.setEditable(false);
        txtRazonamiento.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        reorganizarLayout();
        construirTablero();

        btnNuevaPartida.addActionListener(e -> iniciarModoElegido());
        btnArriesgar.addActionListener(e -> pedirSuposicion());
        btnSugerencia.addActionListener(e -> mostrarSugerencias());

        actualizar();
    }

    /**
     * Arma el layout definitivo con BorderLayout y JScrollPane.
     *
     * El .form define QUE componentes existen. Su GridLayoutManager reparte el
     * espacio en celdas fijas y con 23 tarjetas mas un panel de texto que crece
     * dejaba los botones fuera de pantalla; BorderLayout reparte de forma
     * proporcional y el scroll evita los cortes.
     */
    private void reorganizarLayout() {
        panelPrincipal.removeAll();
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panelPrincipal.setLayout(new BorderLayout(8, 8));

        // --- barra superior: estado a la izquierda, boton a la derecha ---
        JPanel etiquetas = new JPanel(new GridLayout(2, 1));
        etiquetas.add(lblTurno);
        etiquetas.add(lblCandidatos);

        JPanel controles = new JPanel(new BorderLayout(6, 6));
        controles.add(comboModo, BorderLayout.CENTER);
        controles.add(btnNuevaPartida, BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.add(etiquetas, BorderLayout.CENTER);
        superior.add(controles, BorderLayout.EAST);

        // --- columna derecha: preguntas arriba, acciones abajo ---
        JPanel acciones = new JPanel(new GridLayout(2, 1, 4, 4));
        acciones.add(btnArriesgar);
        acciones.add(btnSugerencia);

        JScrollPane scrollFiltros = new JScrollPane(panelFiltros);
        scrollFiltros.setBorder(BorderFactory.createTitledBorder("Preguntas"));

        JPanel derecha = new JPanel(new BorderLayout(4, 8));
        derecha.add(scrollFiltros, BorderLayout.CENTER);
        derecha.add(acciones, BorderLayout.SOUTH);
        derecha.setPreferredSize(new Dimension(270, 0));

        // --- centro: tablero arriba, razonamiento abajo, con divisor movible ---
        JScrollPane scrollTablero = new JScrollPane(panelTablero);
        scrollTablero.setBorder(BorderFactory.createTitledBorder("Tablero"));

        JScrollPane scrollRazonamiento = new JScrollPane(txtRazonamiento);
        scrollRazonamiento.setBorder(
                BorderFactory.createTitledBorder("Razonamiento de la maquina"));

        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollTablero, scrollRazonamiento);
        centro.setResizeWeight(0.55);   // 55% tablero, 45% razonamiento
        centro.setContinuousLayout(true);

        panelPrincipal.add(superior, BorderLayout.NORTH);
        panelPrincipal.add(centro, BorderLayout.CENTER);
        panelPrincipal.add(derecha, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Adivina Quien - TPO Programacion III");
            frame.setContentPane(new VentanaJuego().panelPrincipal);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // -----------------------------------------------------------------
    // CONSTRUCCION DEL TABLERO
    // -----------------------------------------------------------------

    /** Crea una tarjeta por cada uno de los 23 personajes. */
    private void construirTablero() {
        List<Personaje> todos = CatalogoPersonajes.getInstancia().getOrdenDeCarga();

        panelTablero.setLayout(new GridLayout(0, 4, 6, 6));
        panelTablero.removeAll();
        tarjetas.clear();

        for (Personaje p : todos) {
            JLabel tarjeta = new JLabel(textoDeTarjeta(p), SwingConstants.CENTER);
            tarjeta.setOpaque(true);
            tarjeta.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            tarjeta.setPreferredSize(new Dimension(150, 58));

            tarjetas.put(p.getId(), tarjeta);
            panelTablero.add(tarjeta);
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    /** El contenido de una tarjeta, en HTML para poder poner dos renglones. */
    private String textoDeTarjeta(Personaje p) {
        return "<html><center><b>" + p.getNombre() + "</b><br>"
                + "<font size=2>"
                + p.getGenero().getEtiqueta()
                + (p.isCalvo() ? " · calvo" : " · con pelo")
                + (p.isUsaLentes() ? " · lentes" : "")
                + "<br>pelo " + p.getColorPelo().getEtiqueta().toLowerCase()
                + "</font></center></html>";
    }

    /** Rehace los botones de pregunta con los filtros que siguen disponibles. */
    private void construirFiltros() {
        panelFiltros.setLayout(new GridLayout(0, 1, 2, 2));
        panelFiltros.removeAll();

        for (Filtro f : controlador.filtrosDisponibles()) {
            JButton boton = new JButton(f.getDescripcion());
            boton.setEnabled(controlador.esTurnoDelHumano());
            boton.addActionListener(e -> controlador.preguntar(f));
            panelFiltros.add(boton);
        }
        panelFiltros.revalidate();
        panelFiltros.repaint();
    }

    // -----------------------------------------------------------------
    // ACCIONES DEL USUARIO
    // -----------------------------------------------------------------

    /** Ejecuta el modo seleccionado en el desplegable. */
    private void iniciarModoElegido() {
        detenerTemporizador();

        switch (comboModo.getSelectedIndex()) {
            case 0 -> pedirPersonajeYArrancar();
            case 1 -> arrancarMaquinaVsMaquina();
            case 2 -> mostrarSalida(() -> SimulacionEstrategias.main(new String[0]));
            case 3 -> mostrarSalida(() -> VerificacionCatalogo.main(new String[0]));
            default -> { }
        }
    }

    /**
     * Modo maquina vs maquina: las dos juegan solas y un Timer va mostrando un
     * turno por segundo para poder seguir el razonamiento en pantalla.
     *
     * Se usa javax.swing.Timer y no Thread.sleep porque el Timer dispara sus
     * eventos en el mismo hilo de Swing: asi la ventana se sigue redibujando
     * entre turno y turno en vez de quedar congelada.
     */
    private void arrancarMaquinaVsMaquina() {
        registro.limpiar();
        construirTablero();
        controlador.nuevaPartidaEntreMaquinas();

        temporizador = new Timer(1100, e -> {
            if (!controlador.avanzarTurnoDeMaquinas()) detenerTemporizador();
        });
        temporizador.start();
    }

    private void detenerTemporizador() {
        if (temporizador != null && temporizador.isRunning()) temporizador.stop();
    }

    /**
     * Corre una de las clases de analisis y vuelca lo que imprime en el panel
     * de razonamiento, redirigiendo System.out temporalmente. Asi la ventana
     * muestra exactamente la misma salida que la consola, sin duplicar codigo.
     */
    private void mostrarSalida(Runnable analisis) {
        registro.limpiar();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;

        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            analisis.run();
        } finally {
            System.setOut(original);   // se restaura siempre, aunque falle
        }

        registro.registrar(buffer.toString(StandardCharsets.UTF_8));

        lblTurno.setText("Resultado del analisis (la misma salida que en consola)");
        lblCandidatos.setText("");
        btnArriesgar.setEnabled(false);
        btnSugerencia.setEnabled(false);
    }

    /** Le pide a la persona que elija su personaje secreto y arranca la partida. */
    private void pedirPersonajeYArrancar() {
        List<Personaje> todos = CatalogoPersonajes.getInstancia().getOrdenDeCarga();

        Personaje elegido = (Personaje) JOptionPane.showInputDialog(
                panelPrincipal,
                "Elegi tu personaje secreto.\nLa maquina va a tener que adivinarlo.",
                "Nueva partida",
                JOptionPane.QUESTION_MESSAGE,
                null,
                todos.toArray(),
                todos.get(0));

        if (elegido != null) {
            registro.limpiar();
            construirTablero();
            controlador.nuevaPartida(elegido);
        }
    }

    /** Le pide a la persona a quien quiere arriesgar. */
    private void pedirSuposicion() {
        List<Personaje> candidatos = controlador.candidatosDelTablero();
        if (candidatos.isEmpty()) return;

        Personaje elegido = (Personaje) JOptionPane.showInputDialog(
                panelPrincipal,
                "Si te equivocas, perdes la partida.",
                "Arriesgar",
                JOptionPane.WARNING_MESSAGE,
                null,
                candidatos.toArray(),
                candidatos.get(0));

        if (elegido != null) {
            controlador.arriesgar(elegido);
        }
    }

    /**
     * Muestra la evaluacion greedy aplicada al tablero de la persona.
     *
     * No juega por ella: le muestra el mismo calculo que hace la maquina para
     * que pueda comparar su decision con la del algoritmo. Es la funcion que
     * sirve para explicar el criterio greedy en la defensa oral.
     */
    private void mostrarSugerencias() {
        List<EvaluacionFiltro> sugerencias = controlador.sugerencias();
        if (sugerencias.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Evaluacion greedy sobre tus ")
                .append(controlador.candidatosDelTablero().size())
                .append(" candidatos.\n")
                .append("Menor peor caso = mejor pregunta.\n\n");

        for (int i = 0; i < sugerencias.size(); i++) {
            sb.append(i == 0 ? "  -> " : "     ")
                    .append(sugerencias.get(i))
                    .append("\n");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(panelPrincipal, area,
                "Que preguntaria la maquina", JOptionPane.INFORMATION_MESSAGE);
    }

    // -----------------------------------------------------------------
    // LO QUE PIDE EL CONTROLADOR (interfaz Observador)
    // -----------------------------------------------------------------

    /** Redibuja tablero, contadores y botones segun el estado de la partida. */
    @Override
    public void actualizar() {
        boolean hayPartida = controlador.hayPartida();
        boolean puedeJugar = controlador.esTurnoDelHumano();

        if (!hayPartida) {
            lblTurno.setText("Elegi un modo y apreta \"Iniciar\"");
            lblCandidatos.setText("");
        } else if (controlador.esModoMaquinas() && !controlador.termino()) {
            lblTurno.setText("Turno " + (controlador.numeroTurno() + 1)
                    + " - juega " + controlador.nombreEnTurno());
            lblCandidatos.setText("GREEDY: " + controlador.candidatosDelTablero().size()
                    + " candidatos  |  SECUENCIAL: " + controlador.candidatosDelRival());
        } else if (controlador.termino()) {
            lblTurno.setText("Partida terminada. Gano: " + controlador.nombreDelGanador());
            lblCandidatos.setText("");
        } else {
            lblTurno.setText("Turno " + (controlador.numeroTurno() + 1)
                    + " - " + (puedeJugar ? "te toca a vos" : "piensa la maquina"));
            lblCandidatos.setText(controlador.nombreJugadorA() + ": "
                    + controlador.candidatosDelTablero().size() + " candidatos  |  "
                    + controlador.nombreJugadorB() + ": " + controlador.candidatosDelRival());
        }

        pintarTablero();
        construirFiltros();

        btnArriesgar.setEnabled(puedeJugar);
        btnSugerencia.setEnabled(puedeJugar);
    }

    /**
     * Tacha en gris los personajes que ya fueron descartados.
     *
     * Esto es Divide and Conquer hecho visible: los que quedan son el
     * subconjunto que sobrevivio a todas las respuestas; los tachados son las
     * ramas que se descartaron enteras.
     */
    private void pintarTablero() {
        if (!controlador.hayPartida()) {
            for (JLabel t : tarjetas.values()) {
                t.setEnabled(true);
                t.setBackground(Color.WHITE);
                t.setForeground(Color.BLACK);
            }
            return;
        }

        List<Personaje> vivos = controlador.candidatosDelTablero();

        for (Map.Entry<Integer, JLabel> entrada : tarjetas.entrySet()) {
            boolean sigueVivo = vivos.stream().anyMatch(p -> p.getId() == entrada.getKey());
            JLabel t = entrada.getValue();

            t.setEnabled(sigueVivo);
            t.setBackground(sigueVivo ? new Color(220, 245, 220) : new Color(235, 235, 235));
            t.setForeground(sigueVivo ? Color.BLACK : Color.LIGHT_GRAY);
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        registro.registrar(">> " + mensaje);
    }

    @Override
    public void mostrarFinal(String texto) {
        JOptionPane.showMessageDialog(panelPrincipal, texto,
                "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }
}