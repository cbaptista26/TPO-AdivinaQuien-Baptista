package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.Oraculo;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Particion;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;

/**
 * El mismo algoritmo que Jugador, pero con la recursion escrita de forma
 * literal, para mostrar la correspondencia exacta con el esquema de la catedra:
 *
 *   Algoritmo D&C(x)              resolver(candidatos, ...)
 *     if CasoBase(x)                if (candidatos.size() == 1)
 *       return SolucionDirecta(x)     return candidatos.get(0)
 *     else
 *       descomponer(x)              descomponer(candidatos, filtro)
 *       y = D&C(xi)                 resolver(subconjunto, ...)   <- recursion
 *       return combinar(y)          particion.combinar(respuesta)
 *
 * Jugador resuelve turno a turno porque Swing es orientado a eventos y no se
 * puede dejar una llamada recursiva esperando un clic. Son el mismo algoritmo.
 *
 * Recurrencia: T(n) = T(n/2) + Theta(n). Caso de division con a=1, b=2, k=1;
 * como a < b^k queda Theta(n) de computo, y Theta(log n) preguntas, que es la
 * profundidad de la recursion.
 */
public class BuscadorRecursivo {

    private final JugadorMaquina estrategia;
    private final RegistroRazonamiento registro;
    private int preguntas = 0;

    public BuscadorRecursivo(JugadorMaquina estrategia, RegistroRazonamiento registro) {
        this.estrategia = estrategia;
        this.registro = registro;
    }

    public Personaje resolver(Oraculo oraculo) {
        preguntas = 0;
        return resolver(estrategia.getCandidatos(), oraculo, 1);
    }

    public int getPreguntas() { return preguntas; }

    /** D&C(x): identifica al personaje secreto dentro del conjunto de candidatos. */
    private Personaje resolver(List<Personaje> candidatos, Oraculo oraculo, int nivel) {

        String sangria = "  ".repeat(nivel);

        // CasoBase(x) -> SolucionDirecta(x)
        if (candidatos.size() == 1) {
            Personaje solucion = candidatos.get(0);
            registro.registrar(sangria + "CasoBase: queda 1 candidato -> SolucionDirecta = "
                    + solucion.getNombre());
            return solucion;
        }

        if (candidatos.isEmpty()) {
            throw new IllegalStateException("Conjunto vacio: hay respuestas inconsistentes.");
        }

        // Eleccion del filtro: capa greedy.
        estrategia.candidatos = candidatos;
        Jugada jugada = estrategia.jugarTurno();

        if (jugada instanceof Suposicion s) {
            registro.registrar(sangria + "Sin filtros utiles -> arriesgo " + s.candidato().getNombre());
            return s.candidato();
        }

        Filtro filtro = ((PreguntaFiltro) jugada).filtro();

        // descomponer(x)
        Particion particion = Jugador.descomponer(candidatos, filtro);
        registro.registrar(String.format(
                "%sdescomponer(%d candidatos) con \"%s\" -> [cumplen: %d | no cumplen: %d]",
                sangria, candidatos.size(), filtro.getDescripcion(),
                particion.cantidadCumplen(), particion.cantidadNoCumplen()));

        boolean respuesta = oraculo.responder(filtro);
        preguntas++;
        estrategia.filtrosUsados.add(filtro.getClave());

        // combinar: se sigue por una sola rama, la otra se descarta entera.
        List<Personaje> subconjunto = particion.combinar(respuesta);
        registro.registrar(String.format(
                "%scombinar: respuesta %s -> sigo con %d candidato(s), descarto %d",
                sangria, respuesta ? "SI" : "NO",
                subconjunto.size(), candidatos.size() - subconjunto.size()));

        return resolver(subconjunto, oraculo, nivel + 1);   // llamada recursiva
    }
}
