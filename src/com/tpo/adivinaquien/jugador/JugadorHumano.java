package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.CatalogoFiltros;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;

import java.util.ArrayList;
import java.util.List;

/**
 * El jugador humano.
 *
 * No tiene jugarTurno(): la jugada la decide la persona y se la pasa a la
 * Partida desde la vista (un Scanner en consola, un boton en Swing). Lo que si
 * hereda de Jugador es la reduccion de candidatos, asi que su tablero se achica
 * automaticamente con el mismo Divide and Conquer que usa la maquina. El
 * jugador no tiene que ir tachando personajes a mano.
 *
 * Ademas puede pedir una sugerencia: se calcula con el mismo criterio greedy de
 * MaquinaGreedy. Sirve para que en la defensa se pueda mostrar el razonamiento
 * de la maquina aplicado al tablero del humano, sin que la maquina juegue por el.
 */
public class JugadorHumano extends Jugador {

    public JugadorHumano(String nombre, RegistroRazonamiento registro) {
        super(nombre, registro);
    }

    @Override
    public boolean esMaquina() {
        return false;
    }

    @Override
    public String getCriterio() {
        return "Decide la persona.";
    }

    /** Los filtros que todavia no pregunto. */
    public List<Filtro> filtrosDisponibles() {
        List<Filtro> disponibles = new ArrayList<>();
        for (Filtro f : CatalogoFiltros.todos()) {
            if (!filtrosUsados.contains(f.getClave())) {
                disponibles.add(f);
            }
        }
        return disponibles;
    }

    /**
     * Evalua todos los filtros disponibles con el criterio greedy y los
     * devuelve ordenados de mejor a peor. El primero es el que recomendaria la
     * maquina.
     */
    public List<EvaluacionFiltro> sugerencias() {
        List<EvaluacionFiltro> evaluaciones = new ArrayList<>();
        for (Filtro f : filtrosDisponibles()) {
            evaluaciones.add(new EvaluacionFiltro(f, descomponer(candidatos, f)));
        }
        evaluaciones.sort((a, b) -> Integer.compare(a.peorCaso(), b.peorCaso()));
        return evaluaciones;
    }
}
