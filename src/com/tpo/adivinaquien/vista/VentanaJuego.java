package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.app.SimulacionEstrategias;
import com.tpo.adivinaquien.app.VerificacionCatalogo;
import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
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
public final class VentanaJuego implements ControladorPartida.Observador {

    // -----------------------------------------------------------------
    // PALETA: ACADEMIA UMBRALUZ (violeta oscuro + dorado)
    // -----------------------------------------------------------------
    private static final Color FONDO          = new Color(0x15, 0x0F, 0x24);
    private static final Color PANEL          = new Color(0x24, 0x1A, 0x3D);
    private static final Color PANEL_CLARO    = new Color(0x2E, 0x21, 0x4D);
    private static final Color VIOLETA        = new Color(0x4B, 0x2E, 0x83);
    private static final Color VIOLETA_CLARO  = new Color(0x7C, 0x4D, 0xFF);
    private static final Color DORADO         = new Color(0xD4, 0xAF, 0x37);
    private static final Color DORADO_TENUE   = new Color(0x8A, 0x77, 0x3F);
    private static final Color TEXTO          = new Color(0xF2, 0xEA, 0xD9);
    private static final Color TEXTO_TENUE    = new Color(0xB8, 0xA9, 0xD9);
    private static final Color VIVO_FONDO     = new Color(0x32, 0x24, 0x52);
    private static final Color DESCARTADO_FONDO = new Color(0x1C, 0x17, 0x28);
    private static final Color DESCARTADO_TEXTO  = new Color(0x6B, 0x62, 0x7A);
    private static final Font FUENTE_TITULO = new Font(Font.SERIF, Font.BOLD, 15);
    private static final Font FUENTE_BASE   = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    // -----------------------------------------------------------------
    // CAMPOS DEL FORMULARIO (los genera el GUI Designer)
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

        aplicarTema();
        reorganizarLayout();
        construirTablero();

        btnNuevaPartida.addActionListener(e -> iniciarModoElegido());
        btnArriesgar.addActionListener(e -> pedirSuposicion());
        btnSugerencia.addActionListener(e -> mostrarSugerencias());

        actualizar();
    }

    /**
     * Pinta los componentes con la paleta de la Academia Umbraluz (violeta
     * oscuro + dorado). Es puro estilo: no cambia ningun comportamiento, por
     * eso vive separado de crearComponentes() y de reorganizarLayout().
     */
    private void aplicarTema() {
        panelPrincipal.setBackground(FONDO);
        panelTablero.setBackground(FONDO);
        panelFiltros.setBackground(FONDO);

        lblTurno.setForeground(DORADO);
        lblTurno.setFont(FUENTE_TITULO);
        lblCandidatos.setForeground(TEXTO_TENUE);
        lblCandidatos.setFont(FUENTE_BASE);

        for (JButton b : new JButton[]{btnNuevaPartida, btnArriesgar, btnSugerencia}) {
            b.setBackground(VIOLETA);
            b.setForeground(TEXTO);
            b.setFont(FUENTE_BASE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DORADO_TENUE, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        }

        comboModo.setBackground(PANEL_CLARO);
        comboModo.setForeground(TEXTO);
        comboModo.setFont(FUENTE_BASE);
        comboModo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? VIOLETA_CLARO : PANEL_CLARO);
                c.setForeground(TEXTO);
                return c;
            }
        });

        txtRazonamiento.setBackground(FONDO);
        txtRazonamiento.setForeground(TEXTO);
        txtRazonamiento.setCaretColor(DORADO);
    }

    /** Un borde titulado dorado sobre fondo violeta, para las tres secciones del tablero. */
    private static Border bordeTematico(String titulo) {
        Border linea = BorderFactory.createLineBorder(DORADO_TENUE, 1);
        return BorderFactory.createTitledBorder(linea, titulo,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                FUENTE_TITULO.deriveFont(12f), DORADO);
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
        etiquetas.setBackground(FONDO);
        etiquetas.add(lblTurno);
        etiquetas.add(lblCandidatos);

        JPanel controles = new JPanel(new BorderLayout(6, 6));
        controles.setBackground(FONDO);
        controles.add(comboModo, BorderLayout.CENTER);
        controles.add(btnNuevaPartida, BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setBackground(FONDO);
        superior.add(etiquetas, BorderLayout.CENTER);
        superior.add(controles, BorderLayout.EAST);

        // --- columna derecha: preguntas arriba, acciones abajo ---
        JPanel acciones = new JPanel(new GridLayout(2, 1, 4, 4));
        acciones.setBackground(FONDO);
        acciones.add(btnArriesgar);
        acciones.add(btnSugerencia);

        JScrollPane scrollFiltros = new JScrollPane(panelFiltros);
        scrollFiltros.setBorder(bordeTematico("Preguntas"));
        scrollFiltros.getViewport().setBackground(FONDO);

        JPanel derecha = new JPanel(new BorderLayout(4, 8));
        derecha.setBackground(FONDO);
        derecha.add(scrollFiltros, BorderLayout.CENTER);
        derecha.add(acciones, BorderLayout.SOUTH);
        derecha.setPreferredSize(new Dimension(270, 0));

        // --- centro: tablero arriba, razonamiento abajo, con divisor movible ---
        JScrollPane scrollTablero = new JScrollPane(panelTablero);
        scrollTablero.setBorder(bordeTematico("Tablero"));
        scrollTablero.getViewport().setBackground(FONDO);

        JScrollPane scrollRazonamiento = new JScrollPane(txtRazonamiento);
        scrollRazonamiento.setBorder(bordeTematico("Razonamiento de la maquina"));

        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollTablero, scrollRazonamiento);
        centro.setResizeWeight(0.55);   // 55% tablero, 45% razonamiento
        centro.setContinuousLayout(true);
        centro.setBackground(FONDO);
        centro.setBorder(null);

        panelPrincipal.add(superior, BorderLayout.NORTH);
        panelPrincipal.add(centro, BorderLayout.CENTER);
        panelPrincipal.add(derecha, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Tematiza tambien los dialogos (JOptionPane), que Swing arma con
            // su propio Look & Feel y no heredan los colores de panelPrincipal.
            UIManager.put("OptionPane.background", PANEL);
            UIManager.put("Panel.background", PANEL);
            UIManager.put("OptionPane.messageForeground", TEXTO);
            UIManager.put("Button.background", VIOLETA);
            UIManager.put("Button.foreground", TEXTO);
            UIManager.put("List.background", PANEL_CLARO);
            UIManager.put("List.foreground", TEXTO);
            UIManager.put("List.selectionBackground", VIOLETA_CLARO);
            UIManager.put("ComboBox.background", PANEL_CLARO);
            UIManager.put("ComboBox.foreground", TEXTO);
            UIManager.put("ComboBox.selectionBackground", VIOLETA_CLARO);
            UIManager.put("ComboBox.selectionForeground", TEXTO);
            UIManager.put("ComboBox.buttonBackground", PANEL_CLARO);
            UIManager.put("ComboBox.buttonShadow", DORADO_TENUE);
            UIManager.put("ComboBox.buttonDarkShadow", VIOLETA);
            UIManager.put("ComboBox.buttonHighlight", DORADO);
            UIManager.put("TextField.background", PANEL_CLARO);
            UIManager.put("TextField.foreground", TEXTO);

            JFrame frame = new JFrame("Adivina Quien - Academia Umbraluz");
            frame.setContentPane(new VentanaJuego().panelPrincipal);
            frame.getContentPane().setBackground(FONDO);
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
            tarjeta.setBorder(BorderFactory.createLineBorder(DORADO_TENUE));
            tarjeta.setPreferredSize(new Dimension(150, 58));
            tarjeta.setFont(FUENTE_BASE);

            tarjetas.put(p.getId(), tarjeta);
            panelTablero.add(tarjeta);
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    /** El contenido de una tarjeta, en HTML para poder poner dos renglones. */
    private String textoDeTarjeta(Personaje p) {
        String colorTexto = String.format("#%06X", TEXTO.getRGB() & 0xFFFFFF);
        return "<html><center><b>" + p.getNombre() + "</b><br>"
                + "<font size=2 color='" + colorTexto + "'>"
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
            boton.setBackground(VIOLETA);
            boton.setForeground(TEXTO);
            boton.setFont(FUENTE_BASE);
            boton.setFocusPainted(false);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DORADO_TENUE, 1),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
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
                t.setBackground(PANEL);
                t.setForeground(TEXTO);
            }
            return;
        }

        List<Personaje> vivos = controlador.candidatosDelTablero();

        for (Map.Entry<Integer, JLabel> entrada : tarjetas.entrySet()) {
            boolean sigueVivo = vivos.stream().anyMatch(p -> p.getId() == entrada.getKey());
            JLabel t = entrada.getValue();

            t.setEnabled(sigueVivo);
            t.setBackground(sigueVivo ? VIVO_FONDO : DESCARTADO_FONDO);
            t.setForeground(sigueVivo ? TEXTO : DESCARTADO_TEXTO);
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