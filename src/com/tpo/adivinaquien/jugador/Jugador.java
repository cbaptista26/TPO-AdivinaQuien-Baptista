package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.CatalogoFiltros;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Particion;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Esta es la clase donde puse el DIVIDE AND CONQUER del juego.
 *
 * La puse como padre del humano y de las maquinas porque me di cuenta de que el
 * tablero del jugador se achica exactamente igual que el de la maquina: cuando
 * preguntas "es un mago?" y te dicen que no, hay que descartar a las 11 brujas
 * de los dos lados. Lo unico que cambia es quien elige la pregunta.
 *
 * Asi lo mapee al esquema que vimos en clase:
 *
 *   x                  -> el campo 'candidatos', los que todavia pueden ser
 *   CasoBase(x)        -> esCasoBase(), cuando queda uno solo
 *   SolucionDirecta(x) -> solucionDirecta(), ese es el personaje
 *   descomponer(x)     -> descomponer(), separa en cumple / no cumple
 *   combinar           -> recibirRespuesta(), me quedo con un solo grupo
 *
 * Lo que mas me costo entender es donde estaba la recursion, porque no hay
 * ningun metodo que se llame a si mismo. Esta repartida en los turnos:
 * 23 -> 12 -> 6 -> 3 -> 2 -> 1. En BuscadorRecursivo escribi el mismo algoritmo
 * pero con la recursion explicita, para que se vea la equivalencia.
 *
 * Costo: Theta(n) por turno para partir la lista, Theta(log n) turnos.
 */
public abstract class Jugador {

    private final String nombre;

    /** La "x" del esquema: los personajes que todavia pueden ser el secreto. */
    protected List<Personaje> candidatos;

    protected final Set<String> filtrosUsados = new HashSet<>();
    protected final RegistroRazonamiento registro;

    private int preguntasHechas = 0;

    /**
     * El jugador recibe la lista de candidatos, no la va a buscar solo.
     *
     * Antes aca adentro llamaba a CatalogoPersonajes.getInstancia(), o sea que
     * dependia de una clase concreta y encima de un singleton. Lo cambie por
     * inversion de dependencias y la ventaja se ve enseguida: ahora puedo armar
     * una maquina con un catalogo de prueba de 4 personajes y verificar el
     * algoritmo sin tocar el catalogo real. Antes era imposible.
     *
     * @param candidatosIniciales el universo de personajes de la partida
     */
    protected Jugador(String nombre, List<Personaje> candidatosIniciales,
                      RegistroRazonamiento registro) {
        if (candidatosIniciales == null || candidatosIniciales.isEmpty()) {
            throw new IllegalArgumentException("El jugador necesita al menos un candidato.");
        }
        this.nombre = nombre;
        this.registro = registro;
        this.candidatos = new ArrayList<>(candidatosIniciales);
    }

    public String getNombre()              { return nombre; }
    public int getPreguntasHechas()        { return preguntasHechas; }
    public int getCantidadCandidatos()     { return candidatos.size(); }
    public List<Personaje> getCandidatos() { return new ArrayList<>(candidatos); }

    public abstract boolean esMaquina();
    public abstract String getCriterio();

    /**
     * CasoBase(x). Como el catalogo no tiene personajes repetidos, cuando se
     * llega aca el candidato que queda ES el secreto: la suposicion no falla.
     */
    public boolean esCasoBase() {
        return candidatos.size() <= 1;
    }

    /** SolucionDirecta(x). */
    public Jugada solucionDirecta() {
        return new Suposicion(candidatos.get(0));
    }

    /** descomponer(x): parte los candidatos en dos subconjuntos disjuntos. Theta(n). */
    public static Particion descomponer(List<Personaje> candidatos, Filtro filtro) {
        List<Personaje> cumplen = new ArrayList<>();
        List<Personaje> noCumplen = new ArrayList<>();

        for (Personaje p : candidatos) {
            if (filtro.evaluar(p)) cumplen.add(p);
            else                   noCumplen.add(p);
        }
        return new Particion(cumplen, noCumplen);
    }

    /**
     * combinar: se queda con el subconjunto de la respuesta real y descarta el
     * otro entero. Como son disjuntos, el secreto esta en uno solo de los dos.
     */
    public void recibirRespuesta(Filtro filtro, boolean respuesta) {
        int antes = candidatos.size();

        candidatos = new ArrayList<>(descomponer(candidatos, filtro).combinar(respuesta));
        filtrosUsados.add(filtro.getClave());
        preguntasHechas++;

        int despues = candidatos.size();
        int porcentaje = antes == 0 ? 0 : (int) Math.round(100.0 * (antes - despues) / antes);

        registro.registrar(String.format(
                "    [%s] respuesta %s -> combinar: sigo con \"%s\". Candidatos %d -> %d (descarte %d%%)",
                nombre, respuesta ? "SI" : "NO", respuesta ? "cumplen" : "no cumplen",
                antes, despues, porcentaje));
    }

    /**
     * Los filtros que este jugador todavia no pregunto. Lo subi aca porque el
     * calculo es igual para cualquiera, y asi la vista no tiene que preguntar de
     * que tipo es el jugador antes de pedirselo.
     */
    public List<Filtro> filtrosDisponibles() {
        List<Filtro> disponibles = new ArrayList<>();
        for (Filtro f : CatalogoFiltros.todos()) {
            if (!filtrosUsados.contains(f.getClave())) disponibles.add(f);
        }
        return disponibles;
    }

    /**
     * Evalua los filtros disponibles con el criterio greedy y los devuelve de
     * mejor a peor. El primero es el que recomendaria MaquinaGreedy.
     */
    public List<EvaluacionFiltro> sugerencias() {
        List<EvaluacionFiltro> evaluaciones = new ArrayList<>();
        for (Filtro f : filtrosDisponibles()) {
            evaluaciones.add(new EvaluacionFiltro(f, descomponer(candidatos, f)));
        }
        evaluaciones.sort((a, b) -> Integer.compare(a.peorCaso(), b.peorCaso()));
        return evaluaciones;
    }

    /**
     * Que juega este jugador en su turno.
     *
     * Antes las vistas tenian que castear a JugadorMaquina para poder llamar a
     * jugarTurno(), y ademas preguntaban con instanceof si era humano. Eran
     * cuatro casteos. Con este metodo todos responden lo mismo: las maquinas
     * devuelven su jugada y el humano devuelve vacio, que la vista interpreta
     * como "espera a que la persona decida". Sustitucion de Liskov: ahora la
     * vista no necesita saber de que tipo es el jugador.
     */
    public abstract Optional<Jugada> decidirJugada();

    /** Saca un candidato de la lista. Se usa cuando una suposicion falla. */
    public void descartarCandidato(Personaje p) {
        candidatos.removeIf(c -> c.getId() == p.getId());
    }
}
