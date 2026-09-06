package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Particion;

import java.util.ArrayList;
import java.util.List;

/**
 * ===========================================================================
 * LA CAPA GREEDY DEL JUEGO
 * ===========================================================================
 *
 * Divide and Conquer (en JugadorMaquina) dice COMO se achica el conjunto una
 * vez que ya se pregunto. Greedy dice QUE conviene preguntar para que esa
 * reduccion sea lo mas grande posible. Son complementarios, no alternativos.
 *
 * LOS CINCO ELEMENTOS DEL ESQUEMA DE LA CATEDRA
 *
 *   Conjunto de candidatos  los filtros todavia no preguntados
 *                           (JugadorMaquina.filtrosFactibles)
 *
 *   Funcion de seleccion    el filtro cuyo peorCaso() sea el mas chico, es
 *                           decir el que deja la particion mas pareja
 *                           (elegirFiltro, abajo)
 *
 *   Funcion de factibilidad que el filtro no se haya usado y que efectivamente
 *                           separe a los candidatos en dos grupos no vacios
 *                           (JugadorMaquina.filtrosFactibles)
 *
 *   Funcion de solucion     que quede un unico candidato
 *                           (JugadorMaquina.esCasoBase)
 *
 *   Objetivo                minimizar la cantidad de preguntas necesarias
 *
 * POR QUE EL CRITERIO ES EL PEOR CASO (minimax)
 * La maquina no sabe que va a responder el rival. Si eligiera el filtro por el
 * caso favorable, elegiria siempre "tiene el pelo amarillo?" esperando un SI
 * que dejaria 7 candidatos; pero si la respuesta es NO le quedan 16, peor que
 * antes. Como no controla la respuesta, lo unico que puede controlar es que tan
 * mal le puede ir. Por eso minimiza el maximo: minimax.
 *
 * POR QUE ES GREEDY DE VERDAD
 * Decide mirando unicamente el turno actual. No simula que pasaria en los
 * turnos siguientes ni reconsidera preguntas ya hechas: es "corto de vista" a
 * proposito, igual que el algoritmo de cambio de monedas del apunte.
 *
 * SU LIMITACION (hay que decirla, no esconderla)
 * Igual que en el cambio de monedas con el sistema britanico previo a 1971,
 * greedy no garantiza el optimo global en cualquier problema. En este juego se
 * puede demostrar que si lo alcanza: cada pregunta binaria aporta como maximo
 * 1 bit de informacion, asi que ningun algoritmo puede identificar a uno entre
 * 23 personajes en menos de techo(log2 23) = 5 preguntas, y greedy lo logra.
 * La cota inferior teorica y el resultado de greedy coinciden.
 */
public class MaquinaGreedy extends JugadorMaquina {

    /** La evaluacion completa del ultimo turno, para poder mostrarla en consola. */
    private List<EvaluacionFiltro> ultimaEvaluacion = new ArrayList<>();

    public MaquinaGreedy(String nombre, RegistroRazonamiento registro) {
        super(nombre, registro);
    }

    @Override
    public String getCriterio() {
        return "Greedy minimax: elijo el filtro que deja la particion mas pareja "
             + "(minimiza la cantidad de candidatos en el peor caso).";
    }

    /**
     * FUNCION DE SELECCION.
     *
     * Evalua cada filtro factible calculando en cuanto quedaria el conjunto de
     * candidatos si la respuesta fuera la peor posible, y se queda con el
     * minimo de esos peores casos.
     *
     * Complejidad: f filtros x n candidatos = Theta(f * n). Con f = 6 y n <= 23
     * son a lo sumo 138 evaluaciones por turno. Es una busqueda lineal de un
     * minimo: no la partimos recursivamente porque encontrar el minimo de una
     * lista requiere mirar todos sus elementos igual, asi que Divide and
     * Conquer no bajaria el orden (seguiria siendo Theta(f)) y solo agregaria
     * llamadas a la pila. El D&C de este TPO esta en la reduccion de
     * candidatos, que es donde si baja el orden de n a log n.
     */
    @Override
    protected Filtro elegirFiltro(List<Filtro> factibles) {

        ultimaEvaluacion = new ArrayList<>();
        EvaluacionFiltro mejor = null;

        for (Filtro filtro : factibles) {
            Particion particion = descomponer(candidatos, filtro);
            EvaluacionFiltro evaluacion = new EvaluacionFiltro(filtro, particion);
            ultimaEvaluacion.add(evaluacion);

            // Seleccion: menor peor caso. Ante empate se queda con el primero,
            // lo que hace que la maquina sea determinista y reproducible.
            if (mejor == null || evaluacion.peorCaso() < mejor.peorCaso()) {
                mejor = evaluacion;
            }
        }

        explicarDecision(mejor);
        return mejor.getFiltro();
    }

    /**
     * Deja por escrito el razonamiento del turno: que opciones habia, cuanto
     * cortaba cada una y por que gano la elegida. Esto es lo que se muestra en
     * consola para poder defender la decision oralmente.
     */
    private void explicarDecision(EvaluacionFiltro elegida) {
        registro.registrar(String.format(
                "    [%s] evaluo %d filtros sobre %d candidatos:",
                getNombre(), ultimaEvaluacion.size(), candidatos.size()));

        for (EvaluacionFiltro e : ultimaEvaluacion) {
            registro.registrar(String.format("        %s %s",
                    e == elegida ? "->" : "  ", e));
        }

        registro.registrar(String.format(
                "    [%s] SELECCION greedy: \"%s\" porque su peor caso (%d) es el menor. "
                + "Descarta al menos %d candidatos pase lo que pase.",
                getNombre(),
                elegida.getFiltro().getDescripcion(),
                elegida.peorCaso(),
                elegida.descarteMinimo()));
    }

    /** La tabla de evaluacion del ultimo turno, para que la vista Swing la muestre. */
    public List<EvaluacionFiltro> getUltimaEvaluacion() {
        return new ArrayList<>(ultimaEvaluacion);
    }
}
