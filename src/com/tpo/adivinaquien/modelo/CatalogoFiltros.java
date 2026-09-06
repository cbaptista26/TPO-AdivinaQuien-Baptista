package com.tpo.adivinaquien.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabrica de los filtros disponibles en el juego.
 *
 * Son 6 preguntas binarias, no 9: para genero, calvicie y lentes existe un
 * unico filtro por caracteristica, porque la pregunta complementaria
 * ("es mujer?", "tiene pelo?", "no usa lentes?") se responde con el NO de la
 * misma pregunta y aportaria informacion cero. Los tres colores si son filtros
 * separados porque son tres valores posibles, no dos.
 *
 * Con 6 filtros y 23 personajes, el minimo teorico de preguntas para
 * identificar a un personaje es techo(log2(23)) = 5.
 */
public class CatalogoFiltros {

    private CatalogoFiltros() { }

    /** Devuelve una lista nueva con todos los filtros del juego. */
    public static List<Filtro> todos() {
        List<Filtro> filtros = new ArrayList<>();
        filtros.add(new FiltroGenero(Genero.MASCULINO));
        filtros.add(new FiltroCalvo());
        filtros.add(new FiltroLentes());
        filtros.add(new FiltroColorPelo(ColorPelo.COLORADO));
        filtros.add(new FiltroColorPelo(ColorPelo.NEGRO));
        filtros.add(new FiltroColorPelo(ColorPelo.AMARILLO));
        return filtros;
    }
}
