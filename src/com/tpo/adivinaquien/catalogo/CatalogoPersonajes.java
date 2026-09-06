package com.tpo.adivinaquien.catalogo;

import com.tpo.adivinaquien.modelo.ColorPelo;
import com.tpo.adivinaquien.modelo.Genero;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Carga los 23 personajes del juego y arma la lista ordenada de la maquina.
 *
 * ---------------------------------------------------------------------------
 * QUE PIDE EL ENUNCIADO
 * "Los personajes empiezan ordenados unicamente segun su genero y es la maquina
 *  quien debe disponerlos en una lista ordenada de forma autoincremental segun
 *  se agregan los personajes."
 *
 * COMO LO RESOLVIMOS
 * Los personajes entran agrupados solo por genero (primero las mujeres, despues
 * los hombres) y en un orden arbitrario dentro de cada grupo. A medida que se
 * agregan, la maquina hace dos cosas:
 *   1. le asigna un id autoincremental (1, 2, 3, ...), que es su identidad; y
 *   2. lo INSERTA en la posicion que le corresponde dentro de una lista que se
 *      mantiene siempre ordenada por atributos.
 *
 * El punto 2 es lo que se resuelve con un algoritmo: para saber en que posicion
 * va cada personaje nuevo usamos BUSQUEDA BINARIA sobre la parte de la lista ya
 * ordenada. Ver buscarPosicion(), que es Divide and Conquer puro.
 *
 * POR QUE NO MERGESORT (esto lo va a preguntar el profesor)
 * MergeSort necesita el arreglo completo para dividirlo a la mitad. Aca los
 * personajes llegan de a uno, asi que habria que reordenar toda la lista con
 * cada alta: Theta(n log n) por personaje, Theta(n^2 log n) en total. La
 * insercion binaria cuesta Theta(log n) para ubicar la posicion mas Theta(n)
 * para correr los elementos de atras, o sea Theta(n) por alta y Theta(n^2) en
 * total. Con n = 23 son unas 500 operaciones: irrelevante. Elegimos insercion
 * binaria porque es la que corresponde al patron de carga incremental que pide
 * el enunciado, no porque MergeSort sea "malo".
 * ---------------------------------------------------------------------------
 */
public class CatalogoPersonajes {

    /** Cantidad de personajes que exige el enunciado. */
    public static final int CANTIDAD_PERSONAJES = 23;

    private static final CatalogoPersonajes INSTANCIA = new CatalogoPersonajes();

    /** Contador autoincremental: el proximo id libre. */
    private int siguienteId = 1;

    /** Lista tal como llegan: agrupados solo por genero. Es el estado inicial del tablero. */
    private final List<Personaje> ordenDeCarga = new ArrayList<>();

    /** Lista que la maquina mantiene ordenada por atributos, alta por alta. */
    private final List<Personaje> ordenados = new ArrayList<>();

    /** Traza de la insercion binaria, para poder mostrarla en consola. */
    private final List<String> trazaDeCarga = new ArrayList<>();

    private CatalogoPersonajes() {
        cargarPersonajes();
        verificarUnicidad();
    }

    public static CatalogoPersonajes getInstancia() {
        return INSTANCIA;
    }

    // ------------------------------------------------------------------
    // CARGA
    // ------------------------------------------------------------------

    /**
     * Los 23 personajes, agrupados unicamente por genero.
     *
     * El espacio de combinaciones es 2 generos x 2 (calvo) x 2 (lentes) x 3
     * colores = 24. Usamos 23 de esas 24 combinaciones, una sola vez cada una,
     * asi que NO HAY DOS PERSONAJES IGUALES. Esto garantiza que la maquina
     * siempre pueda llegar a un unico candidato y nunca tenga que desempatar
     * al azar. La combinacion que queda afuera es
     * (femenino, calva, con lentes, pelo amarillo).
     */
    private void cargarPersonajes() {
        // --- 11 mujeres ---
        agregar("Alma",      Genero.FEMENINO,  false, false, ColorPelo.COLORADO);
        agregar("Bianca",    Genero.FEMENINO,  false, false, ColorPelo.NEGRO);
        agregar("Carla",     Genero.FEMENINO,  false, false, ColorPelo.AMARILLO);
        agregar("Delfina",   Genero.FEMENINO,  false, true,  ColorPelo.COLORADO);
        agregar("Emma",      Genero.FEMENINO,  false, true,  ColorPelo.NEGRO);
        agregar("Fatima",    Genero.FEMENINO,  false, true,  ColorPelo.AMARILLO);
        agregar("Greta",     Genero.FEMENINO,  true,  false, ColorPelo.COLORADO);
        agregar("Hilda",     Genero.FEMENINO,  true,  false, ColorPelo.NEGRO);
        agregar("Ivana",     Genero.FEMENINO,  true,  false, ColorPelo.AMARILLO);
        agregar("Jimena",    Genero.FEMENINO,  true,  true,  ColorPelo.COLORADO);
        agregar("Keila",     Genero.FEMENINO,  true,  true,  ColorPelo.NEGRO);

        // --- 12 hombres ---
        agregar("Adrian",    Genero.MASCULINO, false, false, ColorPelo.COLORADO);
        agregar("Bautista",  Genero.MASCULINO, false, false, ColorPelo.NEGRO);
        agregar("Ciro",      Genero.MASCULINO, false, false, ColorPelo.AMARILLO);
        agregar("Damian",    Genero.MASCULINO, false, true,  ColorPelo.COLORADO);
        agregar("Ezequiel",  Genero.MASCULINO, false, true,  ColorPelo.NEGRO);
        agregar("Facundo",   Genero.MASCULINO, false, true,  ColorPelo.AMARILLO);
        agregar("Gaspar",    Genero.MASCULINO, true,  false, ColorPelo.COLORADO);
        agregar("Hugo",      Genero.MASCULINO, true,  false, ColorPelo.NEGRO);
        agregar("Ivan",      Genero.MASCULINO, true,  false, ColorPelo.AMARILLO);
        agregar("Joaquin",   Genero.MASCULINO, true,  true,  ColorPelo.COLORADO);
        agregar("Lisandro",  Genero.MASCULINO, true,  true,  ColorPelo.NEGRO);
        agregar("Matias",    Genero.MASCULINO, true,  true,  ColorPelo.AMARILLO);
    }

    /**
     * Da de alta un personaje: le asigna el id autoincremental y lo inserta
     * ordenado. Es el unico lugar donde crece el catalogo.
     */
    private void agregar(String nombre, Genero genero,
                         boolean calvo, boolean usaLentes, ColorPelo colorPelo) {

        Personaje nuevo = new Personaje(siguienteId, nombre, genero, calvo, usaLentes, colorPelo);
        siguienteId++;

        ordenDeCarga.add(nuevo);
        insertarOrdenado(nuevo);
    }

    // ------------------------------------------------------------------
    // INSERCION BINARIA  (Divide and Conquer)
    // ------------------------------------------------------------------

    /**
     * Inserta el personaje en la posicion que le corresponde dentro de la lista
     * ya ordenada.
     *
     * La lista 'ordenados' es un INVARIANTE: antes y despues de este metodo
     * siempre esta ordenada segun Personaje.POR_ATRIBUTOS. Por eso podemos usar
     * busqueda binaria sobre ella aunque todavia no esten cargados los 23.
     */
    private void insertarOrdenado(Personaje nuevo) {
        int posicion = buscarPosicion(nuevo, 0, ordenados.size() - 1, 0);
        ordenados.add(posicion, nuevo);
    }

    /**
     * Busqueda binaria recursiva: devuelve el indice donde debe insertarse el
     * personaje para que la lista siga ordenada.
     *
     * ESQUEMA DIVIDE AND CONQUER (el de la catedra):
     *   CasoBase(x)        -> ini > fin: el rango quedo vacio, la posicion es 'ini'
     *   SolucionDirecta(x) -> devolver 'ini'
     *   descomponer(x)     -> partir el rango en dos mitades por el elemento del medio
     *   combinar           -> no hace falta combinar nada: se sigue por una sola
     *                         mitad, porque el orden garantiza que la posicion
     *                         buscada no puede estar en la otra
     *
     * Complejidad: T(n) = T(n/2) + c, que es el caso de division con a=1, b=2,
     * k=0. Como a = b^k (1 = 2^0), queda Theta(n^k log n) = Theta(log n).
     *
     * @param profundidad solo para la traza que se muestra en consola
     */
    private int buscarPosicion(Personaje nuevo, int ini, int fin, int profundidad) {

        // Caso base: no queda rango para dividir. Aca va el personaje.
        if (ini > fin) {
            trazaDeCarga.add(String.format(
                    "  %-10s -> %d comparacion(es), va a la posicion %d",
                    nuevo.getNombre(), profundidad, ini));
            return ini;
        }

        int medio = (ini + fin) / 2;
        Personaje delMedio = ordenados.get(medio);

        // Descomponer: nos quedamos con una sola mitad del rango.
        if (Personaje.POR_ATRIBUTOS.compare(nuevo, delMedio) < 0) {
            return buscarPosicion(nuevo, ini, medio - 1, profundidad + 1);  // mitad izquierda
        } else {
            return buscarPosicion(nuevo, medio + 1, fin, profundidad + 1);  // mitad derecha
        }
    }

    // ------------------------------------------------------------------
    // VERIFICACION
    // ------------------------------------------------------------------

    /**
     * Chequea que no haya dos personajes con la misma combinacion de atributos.
     * Si fallara, la maquina podria quedar con candidatos empatados y sin
     * ninguna pregunta capaz de separarlos. Preferimos que el programa reviente
     * al arrancar antes que descubrirlo en medio de la defensa.
     */
    private void verificarUnicidad() {
        Set<String> vistas = new HashSet<>();
        for (Personaje p : ordenDeCarga) {
            if (!vistas.add(p.claveAtributos())) {
                throw new IllegalStateException(
                        "Personaje duplicado en el catalogo: " + p.getNombre());
            }
        }
        if (ordenDeCarga.size() != CANTIDAD_PERSONAJES) {
            throw new IllegalStateException(
                    "El catalogo debe tener " + CANTIDAD_PERSONAJES +
                    " personajes y tiene " + ordenDeCarga.size());
        }
    }

    // ------------------------------------------------------------------
    // ACCESO
    // ------------------------------------------------------------------

    /** Copia de la lista en orden de alta (agrupada por genero): el tablero inicial. */
    public List<Personaje> getOrdenDeCarga() {
        return new ArrayList<>(ordenDeCarga);
    }

    /** Copia de la lista ordenada por atributos: la vista interna de la maquina. */
    public List<Personaje> getOrdenados() {
        return new ArrayList<>(ordenados);
    }

    /** Traza paso a paso de la insercion binaria, para mostrar en consola. */
    public List<String> getTrazaDeCarga() {
        return new ArrayList<>(trazaDeCarga);
    }

    public Personaje buscarPorNombre(String nombre) {
        for (Personaje p : ordenDeCarga) {
            if (p.getNombre().equalsIgnoreCase(nombre.trim())) {
                return p;
            }
        }
        return null;
    }

    public int getCantidad() {
        return ordenDeCarga.size();
    }
}
