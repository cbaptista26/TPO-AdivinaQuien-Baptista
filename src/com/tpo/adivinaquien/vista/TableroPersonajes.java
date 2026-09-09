package com.tpo.adivinaquien.vista;

import com.tpo.adivinaquien.modelo.Personaje;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dibuja las 23 tarjetas y las va apagando a medida que se descartan.
 *
 * Esto es el Divide and Conquer hecho visible: lo iluminado es el subconjunto
 * que sobrevivio a todas las respuestas, lo apagado son las ramas que se
 * descartaron enteras. Es lo que muestro si me piden ver el patron funcionando.
 *
 * Lo saque de VentanaJuego, que tenia 557 lineas haciendo cuatro cosas.
 *
 * OJO CON ESTO: la clase NO extiende JPanel, recibe el panel que tiene que
 * llenar. Primero la hice heredando y se rompio la vinculacion del
 * VentanaJuego.form, que declara ese componente como JPanel. Ahi me di cuenta de
 * que la herencia no correspondia: esta clase no necesita SER un panel, necesita
 * DIBUJAR en uno. Composicion en lugar de herencia.
 */
public class TableroPersonajes {

    /** El panel del formulario que esta clase se encarga de llenar. */
    private final JPanel contenedor;

    /**
     * Del id del personaje a su tarjeta. Se usa un HashMap y no una lista
     * porque al repintar hay que ir del id a la tarjeta: es acceso por clave,
     * O(1). Con una lista habria que recorrerla, O(n) por personaje.
     */
    private final Map<Integer, JLabel> tarjetas = new HashMap<>();

    public TableroPersonajes(JPanel contenedor) {
        this.contenedor = contenedor;
        this.contenedor.setLayout(new GridLayout(0, 4, 6, 6));
        this.contenedor.setBackground(PaletaArcana.FONDO);
    }

    /** Crea una tarjeta por cada personaje del universo. */
    public void construir(List<Personaje> personajes) {
        contenedor.removeAll();
        tarjetas.clear();

        for (Personaje p : personajes) {
            JLabel tarjeta = new JLabel(textoDe(p), SwingConstants.CENTER);
            tarjeta.setOpaque(true);
            tarjeta.setBorder(BorderFactory.createLineBorder(PaletaArcana.DORADO_TENUE));
            tarjeta.setPreferredSize(new Dimension(150, 58));
            tarjeta.setFont(PaletaArcana.FUENTE_BASE);

            tarjetas.put(p.getId(), tarjeta);
            contenedor.add(tarjeta);
        }
        contenedor.revalidate();
        contenedor.repaint();
    }

    /**
     * Ilumina los candidatos que siguen en pie y apaga los descartados.
     *
     * Arma primero un Set con los ids vivos: asi cada tarjeta se resuelve con
     * un contains() O(1) y el repintado completo queda en Theta(n). Recorrer la
     * lista de candidatos por cada tarjeta habria costado Theta(n^2).
     */
    public void pintar(List<Personaje> candidatosVivos) {
        Set<Integer> vivos = new HashSet<>();
        for (Personaje p : candidatosVivos) vivos.add(p.getId());

        for (Map.Entry<Integer, JLabel> entrada : tarjetas.entrySet()) {
            boolean sigueVivo = vivos.contains(entrada.getKey());
            JLabel t = entrada.getValue();

            t.setEnabled(sigueVivo);
            t.setBackground(sigueVivo ? PaletaArcana.VIVO_FONDO : PaletaArcana.DESCARTADO_FONDO);
            t.setForeground(sigueVivo ? PaletaArcana.TEXTO : PaletaArcana.DESCARTADO_TEXTO);
        }
    }

    /** Estado inicial: todas las tarjetas encendidas, sin partida en curso. */
    public void reiniciar() {
        for (JLabel t : tarjetas.values()) {
            t.setEnabled(true);
            t.setBackground(PaletaArcana.PANEL);
            t.setForeground(PaletaArcana.TEXTO);
        }
    }

    /** El contenido de una tarjeta, en HTML para poder poner varios renglones. */
    private String textoDe(Personaje p) {
        return "<html><center><b>" + p.getNombre() + "</b><br>"
                + "<font size=2 color='" + PaletaArcana.hex(PaletaArcana.TEXTO) + "'>"
                + p.getGenero().getEtiqueta()
                + (p.isCalvo() ? " · rapado" : " · con pelo")
                + (p.isUsaLentes() ? " · anteojos" : "")
                + "<br>pelo " + p.getColorPelo().getEtiqueta().toLowerCase()
                + "</font></center></html>";
    }
}
