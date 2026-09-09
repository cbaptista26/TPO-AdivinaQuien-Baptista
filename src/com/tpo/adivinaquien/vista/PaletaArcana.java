package com.tpo.adivinaquien.vista;

import java.awt.Color;
import java.awt.Font;

/**
 * Los colores y las tipografias de la Academia Umbraluz.
 *
 * Los tenia sueltos dentro de VentanaJuego, que ademas armaba el layout, dibujaba
 * el tablero y manejaba los cuatro modos. Los saque aca porque cambiar la
 * tematica y cambiar como se comporta la ventana son dos motivos de cambio
 * distintos (responsabilidad unica). Si maniana quiero retematizar el juego,
 * toco solo esta clase.
 */
public final class PaletaArcana {

    private PaletaArcana() { }

    public static final Color FONDO             = new Color(0x15, 0x0F, 0x24);
    public static final Color PANEL             = new Color(0x24, 0x1A, 0x3D);
    public static final Color PANEL_CLARO       = new Color(0x2E, 0x21, 0x4D);
    public static final Color VIOLETA           = new Color(0x4B, 0x2E, 0x83);
    public static final Color VIOLETA_CLARO     = new Color(0x7C, 0x4D, 0xFF);
    public static final Color DORADO            = new Color(0xD4, 0xAF, 0x37);
    public static final Color DORADO_TENUE      = new Color(0x8A, 0x77, 0x3F);
    public static final Color TEXTO             = new Color(0xF2, 0xEA, 0xD9);
    public static final Color TEXTO_TENUE       = new Color(0xB8, 0xA9, 0xD9);
    public static final Color VIVO_FONDO        = new Color(0x32, 0x24, 0x52);
    public static final Color DESCARTADO_FONDO  = new Color(0x1C, 0x17, 0x28);
    public static final Color DESCARTADO_TEXTO  = new Color(0x6B, 0x62, 0x7A);

    public static final Font FUENTE_TITULO = new Font(Font.SERIF, Font.BOLD, 15);
    public static final Font FUENTE_BASE   = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    /** El color en formato HTML, para las etiquetas que usan marcado. */
    public static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }
}
