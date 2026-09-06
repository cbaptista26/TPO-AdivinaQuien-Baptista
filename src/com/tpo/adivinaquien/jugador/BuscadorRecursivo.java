package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.Oraculo;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Particion;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;

/**
 * ===========================================================================
 * EL MISMO ALGORITMO, EN SU FORMA RECURSIVA LITERAL
 * ===========================================================================
 *
 * JugadorMaquina resuelve el juego turno a turno porque la interfaz grafica es
 * orientada a eventos: no se puede dejar una llamada recursiva colgada
 * esperando a que el usuario haga clic en un boton.
 *
 * Esta clase escribe exactamente el mismo algoritmo con la recursion explicita,
 * para poder mostrar la correspondencia uno a uno con el esquema de la catedra.
 * Se usa en el modo Maquina vs Maquina, donde el rival es otro objeto que
 * responde al instante y por lo tanto la recursion puede correr de corrido.
 *
 * ESQUEMA DE LA CATEDRA          ESTE CODIGO
 * ---------------------------    ------------------------------------------
 * Algoritmo D&C(x)               resolver(candidatos, ...)
 *   if CasoBase(x)                 if (candidatos.size() == 1)
 *     return SolucionDirecta(x)      return candidatos.get(0)
 *   else
 *     descomponer(x)               descomponer(candidatos, filtro)
 *     y = D&C(x_i)                 resolver(subconjunto, ...)   <- recursion
 *     return combinar(y)           particion.combinar(respuesta)
 *
 * DIFERENCIA CON EL ESQUEMA GENERICO
 * El esquema generico resuelve TODOS los subproblemas y despues combina sus
 * soluciones. Aca se resuelve uno solo: la respuesta del rival indica en cual
 * de los dos subconjuntos esta el personaje secreto, y el otro se descarta
 * entero sin explorarlo. Es la misma simplificacion que hace la busqueda
 * binaria del apunte, y es lo que baja el orden de Theta(n) a Theta(log n).
 *
 * RECURRENCIA
 *   T(n) = T(n/2) + Theta(n)
 * Caso de division con a=1, b=2, k=1. Como a < b^k (1 < 2), queda
 * T(n) pertenece a Theta(n^k) = Theta(n).
 *
 * El Theta(n) del termino independiente es el costo de descomponer, que
 * recorre los candidatos. La CANTIDAD DE PREGUNTAS, que es lo que le importa
 * al juego, es la profundidad de la recursion: Theta(log n).
 */
public class BuscadorRecursivo {

    private final JugadorMaquina estrategia;
    private final RegistroRazonamiento registro;
    private int preguntas = 0;

    public BuscadorRecursivo(JugadorMaquina estrategia, RegistroRazonamiento registro) {
        this.estrategia = estrategia;
        this.registro = registro;
    }

    /** Punto de entrada: arranca con el conjunto completo de candidatos. */
    public Personaje resolver(Oraculo oraculo) {
        preguntas = 0;
        return resolver(estrategia.getCandidatos(), oraculo, 1);
    }

    /**
     * D&C(x): identifica al personaje secreto dentro del conjunto de candidatos.
     *
     * @param candidatos la "x" del esquema
     * @param oraculo    la unica forma de obtener informacion del rival
     * @param nivel      profundidad de la recursion, solo para la traza
     */
    private Personaje resolver(List<Personaje> candidatos, Oraculo oraculo, int nivel) {

        String sangria = "  ".repeat(nivel);

        // -------- CasoBase(x) --------
        if (candidatos.size() == 1) {
            Personaje solucion = candidatos.get(0);
            registro.registrar(sangria + "CasoBase: queda 1 candidato -> SolucionDirecta = "
                    + solucion.getNombre());
            return solucion;                                   // SolucionDirecta(x)
        }

        if (candidatos.isEmpty()) {
            throw new IllegalStateException(
                    "Conjunto de candidatos vacio: hay una inconsistencia en las respuestas.");
        }

        // -------- Eleccion del filtro (capa greedy) --------
        estrategia.candidatos = candidatos;
        Jugada jugada = estrategia.jugarTurno();

        if (jugada instanceof Suposicion s) {
            registro.registrar(sangria + "Sin filtros utiles -> arriesgo " + s.candidato().getNombre());
            return s.candidato();
        }

        Filtro filtro = ((PreguntaFiltro) jugada).filtro();

        // -------- descomponer(x) --------
        Particion particion = Jugador.descomponer(candidatos, filtro);
        registro.registrar(String.format(
                "%sdescomponer(%d candidatos) con \"%s\" -> [cumplen: %d | no cumplen: %d]",
                sangria, candidatos.size(), filtro.getDescripcion(),
                particion.cantidadCumplen(), particion.cantidadNoCumplen()));

        // -------- se obtiene la respuesta real --------
        boolean respuesta = oraculo.responder(filtro);
        preguntas++;
        estrategia.filtrosUsados.add(filtro.getClave());

        // -------- combinar --------
        List<Personaje> subconjunto = particion.combinar(respuesta);
        registro.registrar(String.format(
                "%scombinar: respuesta %s -> sigo con %d candidato(s), descarto %d",
                sangria, respuesta ? "SI" : "NO",
                subconjunto.size(), candidatos.size() - subconjunto.size()));

        // -------- llamada recursiva sobre el subproblema --------
        return resolver(subconjunto, oraculo, nivel + 1);
    }

    /** Cuantas preguntas necesito la ultima resolucion. */
    public int getPreguntas() {
        return preguntas;
    }
}
