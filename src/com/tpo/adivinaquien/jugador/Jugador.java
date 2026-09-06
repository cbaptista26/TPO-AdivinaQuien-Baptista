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
 * ===========================================================================
 * LA CAPA DIVIDE AND CONQUER DEL JUEGO
 * ===========================================================================
 *
 * Base comun del jugador humano y de las maquinas. Esta aca y no en
 * JugadorMaquina porque el tablero del humano se achica exactamente igual: al
 * recibir una respuesta, tacha los personajes que ya no pueden ser. Es el mismo
 * algoritmo, lo unico que cambia es quien elige la pregunta.
 *
 * ESQUEMA DE LA CATEDRA APLICADO AL TPO
 * Es el mismo del "numero secreto" del apunte, cambiando un rango de numeros
 * por un conjunto de personajes:
 *
 *   x                  = el conjunto de personajes candidatos en este momento
 *   CasoBase(x)        = queda un unico candidato
 *   SolucionDirecta(x) = ese candidato es la respuesta; se lanza la suposicion
 *   descomponer(x)     = separar en "cumple el filtro" / "no cumple"
 *   combinar           = seguir solo con el subconjunto que corresponde a la
 *                        respuesta real que dio el rival
 *
 * DONDE ESTA LA RECURSION
 * No ocurre dentro de un metodo: ocurre a lo largo de los turnos. Cada turno
 * resuelve un subproblema del mismo tipo que el anterior, pero con la mitad
 * (aproximadamente) de los candidatos:
 *
 *   turno 1: 23 candidatos -> turno 2: 12 -> turno 3: 6
 *   turno 4:  3 candidatos -> turno 5:  2 -> turno 6: 1 = caso base
 *
 * La clase BuscadorRecursivo escribe el mismo algoritmo en su forma recursiva
 * literal. Aca esta desenrollado en turnos porque la interfaz grafica es
 * orientada a eventos: no se puede dejar una llamada recursiva esperando a que
 * el usuario haga clic. Son el mismo algoritmo, no dos distintos.
 *
 * COMPLEJIDAD
 * Cada turno cuesta Theta(n) (recorrer los candidatos para partirlos) y reduce
 * el conjunto a la mitad. La cantidad de turnos es Theta(log n): con 23
 * personajes, techo(log2 23) = 5 preguntas mas la suposicion final. Contra la
 * fuerza bruta (probar personaje por personaje), que es Theta(n) = 23 intentos.
 */
public abstract class Jugador {

    private final String nombre;

    /** El subconjunto de personajes que todavia puede ser el secreto: la "x" del esquema. */
    protected List<Personaje> candidatos;

    /** Claves de los filtros que este jugador ya pregunto. */
    protected final Set<String> filtrosUsados = new HashSet<>();

    /** Por donde cuenta lo que va haciendo. */
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
    public Set<String> getFiltrosUsados()  { return new HashSet<>(filtrosUsados); }

    /** True si es una maquina. Lo usan las vistas para saber si pedir input. */
    public abstract boolean esMaquina();

    /** Descripcion del criterio con el que juega. */
    public abstract String getCriterio();

    // ==================================================================
    // CASO BASE  y  SOLUCION DIRECTA
    // ==================================================================

    /**
     * CasoBase(x): queda un solo candidato posible.
     *
     * Gracias a que el catalogo no tiene personajes repetidos, cuando se llega
     * aca el candidato que queda ES el personaje secreto: la suposicion acierta
     * siempre. No hay desempate al azar en ningun momento del juego.
     */
    public boolean esCasoBase() {
        return candidatos.size() <= 1;
    }

    /** SolucionDirecta(x): el unico candidato que queda es la respuesta. */
    public Jugada solucionDirecta() {
        return new Suposicion(candidatos.get(0));
    }

    // ==================================================================
    // DESCOMPONER  y  COMBINAR
    // ==================================================================

    /**
     * descomponer(x): parte el conjunto de candidatos en dos subconjuntos
     * disjuntos segun cumplan o no el filtro.
     *
     * Costo: Theta(n), una pasada por los candidatos.
     */
    public static Particion descomponer(List<Personaje> candidatos, Filtro filtro) {
        List<Personaje> cumplen = new ArrayList<>();
        List<Personaje> noCumplen = new ArrayList<>();

        for (Personaje p : candidatos) {
            if (filtro.evaluar(p)) {
                cumplen.add(p);
            } else {
                noCumplen.add(p);
            }
        }
        return new Particion(cumplen, noCumplen);
    }

    /**
     * combinar(): recibe la respuesta real del rival y se queda con el
     * subconjunto que corresponde, descartando el otro por completo.
     *
     * Este es el paso que hace que el problema se achique de verdad. Los dos
     * subconjuntos son disjuntos, asi que el personaje secreto esta en uno solo
     * de los dos y el otro se puede tirar entero sin riesgo.
     */
    public void recibirRespuesta(Filtro filtro, boolean respuesta) {
        int antes = candidatos.size();

        Particion particion = descomponer(candidatos, filtro);
        candidatos = new ArrayList<>(particion.combinar(respuesta));

        filtrosUsados.add(filtro.getClave());
        preguntasHechas++;

        int despues = candidatos.size();
        int porcentaje = antes == 0 ? 0 : (int) Math.round(100.0 * (antes - despues) / antes);

        registro.registrar(String.format(
                "    [%s] respuesta %s -> combinar: sigo con el subconjunto \"%s\". "
                + "Candidatos %d -> %d (descarte %d%%)",
                nombre,
                respuesta ? "SI" : "NO",
                respuesta ? "cumplen" : "no cumplen",
                antes, despues, porcentaje));
    }

    /** Saca un candidato de la lista. Se usa cuando una suposicion falla. */
    public void descartarCandidato(Personaje p) {
        candidatos.removeIf(c -> c.getId() == p.getId());
    }
}
