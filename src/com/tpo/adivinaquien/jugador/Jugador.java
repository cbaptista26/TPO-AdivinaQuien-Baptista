package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Particion;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DIVIDE AND CONQUER. Base comun del jugador humano y de las maquinas: el
 * tablero del humano se achica con el mismo algoritmo que usa la maquina.
 *
 * Esquema de la catedra aplicado al TPO:
 *   x                  = conjunto de personajes candidatos  -> campo 'candidatos'
 *   CasoBase(x)        = queda un unico candidato           -> esCasoBase()
 *   SolucionDirecta(x) = ese candidato es la respuesta      -> solucionDirecta()
 *   descomponer(x)     = separar en cumple / no cumple      -> descomponer()
 *   combinar           = seguir con el subconjunto correcto -> recibirRespuesta()
 *
 * La recursion ocurre a lo largo de los turnos: 23 -> 12 -> 6 -> 3 -> 2 -> 1.
 * Complejidad: Theta(n) por turno, Theta(log n) turnos.
 * Ver seccion 2 de la documentacion.
 */
public abstract class Jugador {

    private final String nombre;

    /** La "x" del esquema: los personajes que todavia pueden ser el secreto. */
    protected List<Personaje> candidatos;

    protected final Set<String> filtrosUsados = new HashSet<>();
    protected final RegistroRazonamiento registro;

    private int preguntasHechas = 0;

    protected Jugador(String nombre, RegistroRazonamiento registro) {
        this.nombre = nombre;
        this.registro = registro;
        this.candidatos = CatalogoPersonajes.getInstancia().getOrdenados();
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

    /** Saca un candidato de la lista. Se usa cuando una suposicion falla. */
    public void descartarCandidato(Personaje p) {
        candidatos.removeIf(c -> c.getId() == p.getId());
    }
}
