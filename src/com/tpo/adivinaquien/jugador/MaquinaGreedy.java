package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;

import java.util.ArrayList;
import java.util.List;

/**
 * GREEDY. Divide and Conquer (en Jugador) dice COMO se achica el conjunto;
 * greedy dice QUE conviene preguntar para que esa reduccion sea la mayor
 * posible. Son complementarios, no alternativos.
 *
 * Los cinco elementos del esquema de la catedra:
 *   Candidatos    = los filtros no preguntados      -> filtrosFactibles()
 *   Seleccion     = el de menor peor caso           -> elegirFiltro()
 *   Factibilidad  = no usado y que separe de verdad -> filtrosFactibles()
 *   Solucion      = queda un unico candidato        -> esCasoBase()
 *   Objetivo      = minimizar la cantidad de preguntas
 *
 * Ver seccion 3 de la documentacion para la justificacion del criterio minimax
 * y la discusion sobre por que alcanza el optimo en este catalogo.
 */
public class MaquinaGreedy extends JugadorMaquina {

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
     * FUNCION DE SELECCION: minimax.
     *
     * Como la maquina no controla la respuesta del rival, no puede optimizar el
     * caso favorable; lo unico que puede controlar es que tan mal le puede ir.
     * Por eso se queda con el filtro cuyo peor caso sea el menor.
     *
     * Complejidad: Theta(f * n). Es una busqueda lineal de un minimo: no se
     * parte recursivamente porque hallar el minimo exige mirar todos los
     * elementos igual, asi que D&C no bajaria el orden.
     */
    @Override
    protected Filtro elegirFiltro(List<Filtro> factibles) {

        ultimaEvaluacion = new ArrayList<>();
        EvaluacionFiltro mejor = null;

        for (Filtro filtro : factibles) {
            EvaluacionFiltro e = new EvaluacionFiltro(filtro, descomponer(candidatos, filtro));
            ultimaEvaluacion.add(e);

            // Ante empate se queda con el primero: la maquina es determinista.
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
