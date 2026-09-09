package com.tpo.adivinaquien.modelo;

import java.util.Comparator;

/**
 * Un personaje del tablero.
 *
 * Todos los campos son 'final': una vez creado el personaje no se puede
 * modificar. Esto es a proposito. Como la maquina va armando y descartando
 * sublistas de personajes todo el tiempo, si alguna parte del programa pudiera
 * cambiarle un atributo a un personaje ya cargado, las decisiones que la
 * maquina tomo en turnos anteriores dejarian de ser validas.
 */
public class Personaje {

    /** Identificador autoincremental que asigna la maquina al cargarlo. */
    private final int id;

    private final String nombre;
    private final Genero genero;
    private final boolean calvo;
    private final boolean usaLentes;
    private final ColorPelo colorPelo;

    public Personaje(int id, String nombre, Genero genero,
                     boolean calvo, boolean usaLentes, ColorPelo colorPelo) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.calvo = calvo;
        this.usaLentes = usaLentes;
        this.colorPelo = colorPelo;
    }

    public int getId()              { return id; }
    public String getNombre()       { return nombre; }
    public Genero getGenero()       { return genero; }
    public boolean isCalvo()        { return calvo; }
    public boolean isUsaLentes()    { return usaLentes; }
    public ColorPelo getColorPelo() { return colorPelo; }

    /**
     * Con que criterio ordeno la lista: genero, despues color de pelo, despues
     * calvicie y por ultimo anteojos.
     *
     * Lo importante es que es un ORDEN TOTAL. Como no hay dos personajes con la
     * misma combinacion de atributos, este comparador nunca devuelve 0 para dos
     * personajes distintos, asi que cada uno tiene una posicion unica. Eso es lo
     * que me deja usar busqueda binaria sin ambiguedad.
     */
    public static final Comparator<Personaje> POR_ATRIBUTOS =
            Comparator.comparing(Personaje::getGenero)
                      .thenComparing(Personaje::getColorPelo)
                      .thenComparing(Personaje::isCalvo)
                      .thenComparing(Personaje::isUsaLentes);

    /** Devuelve la combinacion de atributos como texto. Sirve para verificar unicidad. */
    public String claveAtributos() {
        return genero + "|" + calvo + "|" + usaLentes + "|" + colorPelo;
    }

    @Override
    public String toString() {
        return String.format("[%2d] %-20s | %-6s | %-8s | %-10s | pelo %s",
                id,
                nombre,
                genero.getEtiqueta(),
                calvo ? "Calvo" : "Con pelo",
                usaLentes ? "Con lentes" : "Sin lentes",
                colorPelo.getEtiqueta().toLowerCase());
    }
}
