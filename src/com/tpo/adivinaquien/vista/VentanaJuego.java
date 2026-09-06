package com.tpo.adivinaquien.vista;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ===========================================================================
 * LA VISTA SWING
 * ===========================================================================
 * <p>
 * Igual que JuegoConsola, esta clase es UNICAMENTE presentacion: dibuja
 * componentes y escucha clics. No decide nada del juego. Cada vez que el
 * usuario hace algo, se lo pasa a ControladorPartida, que llama al motor.
 * <p>
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

    /**
     * Una tarjeta por personaje, para poder tacharla cuando se descarta.
     */
    private final Map<Integer, JLabel> tarjetas = new HashMap<>();

    // -----------------------------------------------------------------
    // ARRANQUE
    // -----------------------------------------------------------------

    public VentanaJuego() {
        registro = new RegistroSwing(txtRazonamiento);
        controlador = new ControladorPartida(registro, this);

        txtRazonamiento.setEditable(false);
        txtRazonamiento.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        reorganizarLayout();
        construirTablero();

        btnNuevaPartida.addActionListener(e -> pedirPersonajeYArrancar());
        btnArriesgar.addActionListener(e -> pedirSuposicion());
        btnSugerencia.addActionListener(e -> mostrarSugerencias());

        actualizar();
    }

    /**
     * Reacomoda los componentes que creo el GUI Designer.
     * <p>
     * POR QUE ESTA ESTE METODO
     * El .form define QUE componentes existen y con que nombre; eso es lo que
     * se disenia visualmente. Pero el GridLayoutManager del disenador reparte
     * el espacio en celdas fijas, y con un tablero de 23 tarjetas mas un panel
     * de razonamiento que crece, los botones de pregunta quedaban fuera de la
     * pantalla.
     * <p>
     * Aca se toman esos mismos componentes y se reorganizan con BorderLayout y
     * JScrollPane, que reparten el espacio de forma proporcional y agregan
     * barras de desplazamiento cuando hace falta. Asi la ventana se ve bien en
     * cualquier resolucion, sin tocar el .form.
     */
    private void reorganizarLayout() {
        panelPrincipal.removeAll();
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panelPrincipal.setLayout(new BorderLayout(8, 8));

        // --- barra superior: estado a la izquierda, boton a la derecha ---
        JPanel etiquetas = new JPanel(new GridLayout(2, 1));
        etiquetas.add(lblTurno);
        etiquetas.add(lblCandidatos);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.add(etiquetas, BorderLayout.CENTER);
        superior.add(btnNuevaPartida, BorderLayout.EAST);

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

    /**
     * Crea una tarjeta por cada uno de los 23 personajes.
     */
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

    /**
     * El contenido de una tarjeta, en HTML para poder poner dos renglones.
     */
    private String textoDeTarjeta(Personaje p) {
        return "<html><center><b>" + p.getNombre() + "</b><br>"
                + "<font size=2>"
                + p.getGenero().getEtiqueta()
                + (p.isCalvo() ? " · calvo" : " · con pelo")
                + (p.isUsaLentes() ? " · lentes" : "")
                + "<br>pelo " + p.getColorPelo().getEtiqueta().toLowerCase()
                + "</font></center></html>";
    }

    /**
     * Rehace los botones de pregunta con los filtros que siguen disponibles.
     */
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

    /**
     * Le pide a la persona que elija su personaje secreto y arranca la partida.
     */
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

    /**
     * Le pide a la persona a quien quiere arriesgar.
     */
    private void pedirSuposicion() {
        List<Personaje> candidatos = controlador.candidatosDelHumano();
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
     * <p>
     * No juega por ella: le muestra el mismo calculo que hace la maquina para
     * que pueda comparar su decision con la del algoritmo. Es la funcion que
     * sirve para explicar el criterio greedy en la defensa oral.
     */
    private void mostrarSugerencias() {
        List<EvaluacionFiltro> sugerencias = controlador.sugerencias();
        if (sugerencias.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Evaluacion greedy sobre tus ")
                .append(controlador.candidatosDelHumano().size())
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

    /**
     * Redibuja tablero, contadores y botones segun el estado de la partida.
     */
    @Override
    public void actualizar() {
        boolean hayPartida = controlador.hayPartida();
        boolean puedeJugar = controlador.esTurnoDelHumano();

        if (!hayPartida) {
            lblTurno.setText("Apreta \"Nueva partida\" para empezar");
            lblCandidatos.setText("");
        } else if (controlador.termino()) {
            lblTurno.setText("Partida terminada. Gano: " + controlador.nombreDelGanador());
            lblCandidatos.setText("");
        } else {
            lblTurno.setText("Turno " + (controlador.numeroTurno() + 1)
                    + " - " + (puedeJugar ? "te toca a vos" : "piensa la maquina"));
            lblCandidatos.setText("Vos: " + controlador.candidatosDelHumano().size()
                    + " candidatos  |  Maquina: " + controlador.candidatosDeLaMaquina());
        }

        pintarTablero();
        construirFiltros();

        btnArriesgar.setEnabled(puedeJugar);
        btnSugerencia.setEnabled(puedeJugar);
    }

    /**
     * Tacha en gris los personajes que ya fueron descartados.
     * <p>
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

        List<Personaje> vivos = controlador.candidatosDelHumano();

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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.setBorder(BorderFactory.createTitledBorder(null, "panelPrincipal", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lblTurno = new JLabel();
        lblTurno.setText("Turno");
        panelPrincipal.add(lblTurno, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblCandidatos = new JLabel();
        lblCandidatos.setText("Candidatos: 23");
        panelPrincipal.add(lblCandidatos, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnNuevaPartida = new JButton();
        btnNuevaPartida.setText("Nueva partida");
        panelPrincipal.add(btnNuevaPartida, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelTablero = new JPanel();
        panelTablero.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panelTablero, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        txtRazonamiento = new JTextArea();
        panelPrincipal.add(txtRazonamiento, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        btnArriesgar = new JButton();
        btnArriesgar.setText("Arriesgar");
        panelPrincipal.add(btnArriesgar, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSugerencia = new JButton();
        btnSugerencia.setText("¿Qué preguntaría la máquina?");
        panelPrincipal.add(btnSugerencia, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelFiltros = new JPanel();
        panelFiltros.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panelFiltros, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelPrincipal;
    }
}