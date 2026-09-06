package com.tpo.adivinaquien.modelo;

/**
 * Los tres colores de pelo que pide el enunciado.
 *
 * DECISION DE DISENIO (se justifica en la documentacion):
 * un personaje calvo TAMBIEN tiene un color de pelo asignado, que se interpreta
 * como el color de sus cejas, patillas o barba.
 *
 * Motivo: si "calvo" anulara el color de pelo, las combinaciones distintas
 * posibles bajarian de 24 a 16. Con 23 personajes en el catalogo eso obligaria
 * a tener personajes con atributos identicos, y la maquina quedaria con varios
 * candidatos empatados sin ninguna pregunta capaz de separarlos: tendria que
 * elegir al azar. Manteniendo el color en los calvos, el espacio es de 24
 * combinaciones y los 23 personajes pueden ser todos unicos.
 */
public enum ColorPelo {

    COLORADO("Colorado"),
    NEGRO("Negro"),
    AMARILLO("Amarillo");

    private final String etiqueta;

    ColorPelo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto lindo para mostrar en consola o en la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
