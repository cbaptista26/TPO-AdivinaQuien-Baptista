package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta es la maquina que juega bien: la parte GREEDY del TP.
 *
 * Divide and Conquer (en Jugador) se encarga de COMO achico la lista una vez que
 * me respondieron. Esta clase decide QUE conviene preguntar para que esa
 * reduccion sea lo mas grande posible. Al principio los tenia mezclados y no
 * podia explicar donde estaba cada patron; separarlos me ordeno todo.
 *
 * Los cinco elementos del esquema greedy, ubicados en el codigo:
 *
 *   Candidatos    -> los filtros que todavia no pregunte  (filtrosFactibles)
 *   Seleccion     -> el de menor peor caso                (elegirFiltro)
 *   Factibilidad  -> que no este usado y que separe       (filtrosFactibles)
 *   Solucion      -> queda un solo personaje              (esCasoBase)
 *   Objetivo      -> usar la menor cantidad de preguntas
 *
 * En la seccion 5 de la documentacion explico por que el criterio es el peor
 * caso y por que en mi catalogo llega al optimo.
 */
public class MaquinaGreedy extends JugadorMaquina {

    private List<EvaluacionFiltro> ultimaEvaluacion = new ArrayList<>();

    public MaquinaGreedy(String nombre, List<Personaje> candidatosIniciales,
                         RegistroRazonamiento registro) {
        super(nombre, candidatosIniciales, registro);
    }

    @Override
    public String getCriterio() {
        return "Greedy minimax: elijo el filtro que deja la particion mas pareja "
             + "(minimiza la cantidad de candidatos en el peor caso).";
    }

    /**
     * La funcion de seleccion. Elijo por el PEOR caso, no por el mejor.
     *
     * Razon: la maquina no sabe que le van a contestar. Si eligiera pensando en
     * que le va a ir bien, preguntaria "tiene el pelo amarillo?" esperando un si
     * que deja 7 candidatos; pero si le dicen que no le quedan 16, peor que
     * antes de preguntar. Como no controlo la respuesta, lo unico que puedo
     * controlar es que tan mal me puede ir. Eso es minimax.
     *
     * Cuesta Theta(f * n): con 6 filtros y 23 candidatos son 138 evaluaciones
     * por turno como mucho. Es una busqueda lineal de un minimo y NO la parti
     * recursivamente, porque para encontrar el minimo hay que mirar todos los
     * elementos igual: D&C no bajaria el orden y solo agregaria llamadas.
     */
    @Override
    protected Filtro elegirFiltro(List<Filtro> factibles) {

        ultimaEvaluacion = new ArrayList<>();
        EvaluacionFiltro mejor = null;

        for (Filtro filtro : factibles) {
            EvaluacionFiltro e = new EvaluacionFiltro(filtro, descomponer(candidatos, filtro));
            ultimaEvaluacion.add(e);

            // Si dos filtros empatan me quedo con el primero de la lista. Lo
            // decidi asi para que la maquina sea determinista: con el mismo
            // personaje secreto siempre juega igual, y eso me deja reproducir
            // una partida para mostrarla. Si desempatara al azar, cada corrida
            // daria distinto y no podria compararlas.
            if (mejor == null || e.peorCaso() < mejor.peorCaso()) mejor = e;
        }

        explicarDecision(mejor);
        return mejor.getFiltro();
    }

    /** Deja por escrito el razonamiento del turno para poder defenderlo. */
    private void explicarDecision(EvaluacionFiltro elegida) {
        registro.registrar(String.format("    [%s] evaluo %d filtros sobre %d candidatos:",
                getNombre(), ultimaEvaluacion.size(), candidatos.size()));

        for (EvaluacionFiltro e : ultimaEvaluacion) {
            registro.registrar(String.format("        %s %s", e == elegida ? "->" : "  ", e));
        }

        registro.registrar(String.format(
                "    [%s] SELECCION greedy: \"%s\" porque su peor caso (%d) es el menor. "
                + "Descarta al menos %d candidatos pase lo que pase.",
                getNombre(), elegida.getFiltro().getDescripcion(),
                elegida.peorCaso(), elegida.descarteMinimo()));
    }
}
