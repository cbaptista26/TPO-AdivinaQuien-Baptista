package com.tpo.adivinaquien.app;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.catalogo.OrdenadorPersonajes;
import com.tpo.adivinaquien.modelo.ColorPelo;
import com.tpo.adivinaquien.modelo.Genero;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

/**
 * Mide los tiempos de MergeSort contra dos algoritmos cuadraticos (burbujeo e
 * insercion simple) sobre la misma lista.
 *
 * Se mide primero con los 23 personajes reales del juego, que es el tamanio de
 * entrada del TPO, y despues con listas mas grandes para ver a partir de que
 * punto la diferencia entre n log n y n^2 se vuelve significativa.
 *
 * DETALLE DE METODO: con 23 elementos una sola ejecucion tarda menos que la
 * resolucion del reloj, asi que se repite muchas veces y se divide. Ademas se
 * hace una tanda previa de calentamiento, porque la JVM compila el codigo a
 * medida que lo ve ejecutarse y las primeras corridas siempre son mas lentas.
 */
public class BenchmarkOrdenamiento {

    private static final Comparator<Personaje> CRITERIO = Personaje.POR_ATRIBUTOS;

    public static void main(String[] args) {

        titulo("TIEMPOS DE ORDENAMIENTO");
        System.out.println("  Criterio: genero, color de pelo, calvicie, lentes.");
        System.out.println("  Cada valor es el promedio de muchas repeticiones,");
        System.out.println("  con calentamiento previo de la JVM.");

        // ---- 1. El caso real del TPO: los 23 personajes del juego ----
        List<Personaje> comoLlegan = CatalogoPersonajes.getInstancia().getOrdenDeCarga();

        titulo("CASO REAL DEL TPO: n = 23, tal como llegan (agrupados por genero)");
        medirTanda(comoLlegan, 20000);

        // ---- 1b. Los mismos 23, pero desordenados ----
        // El enunciado dice que los personajes llegan agrupados por genero, o
        // sea PARCIALMENTE ORDENADOS. Esa es la mejor entrada posible para los
        // algoritmos cuadraticos, asi que conviene medir tambien con la lista
        // mezclada para ver cuanto de la ventaja viene del orden de entrada.
        List<Personaje> mezclados = new ArrayList<>(comoLlegan);
        Collections.shuffle(mezclados, new Random(7));

        titulo("LOS MISMOS 23, PERO DESORDENADOS AL AZAR");
        medirTanda(mezclados, 20000);

        // ---- 2. Tamanios mayores, para ver la tendencia ----
        titulo("TAMANIOS MAYORES (personajes generados al azar)");
        System.out.printf("  %-8s %-14s %-14s %-14s %s%n",
                "n", "MergeSort", "Burbujeo", "Insercion", "Burbujeo/MergeSort");
        System.out.println("  " + "-".repeat(70));

        for (int n : new int[]{23, 100, 500, 2000, 5000}) {
            List<Personaje> datos = generar(n);
            int reps = n <= 100 ? 2000 : (n <= 500 ? 200 : 20);

            double m = medir(datos, OrdenadorPersonajes::mergeSort, reps);
            double b = medir(datos, OrdenadorPersonajes::burbujeo, reps);
            double i = medir(datos, OrdenadorPersonajes::insercionSimple, reps);

            System.out.printf("  %-8d %-14s %-14s %-14s %.1fx%n",
                    n, ms(m), ms(b), ms(i), b / m);
        }

        titulo("CONCLUSION");
        System.out.println("  1. Con n = 23 la diferencia es de microsegundos. Para el tamanio");
        System.out.println("     del TPO los tres algoritmos son equivalentes en la practica:");
        System.out.println("     elegir uno u otro no cambia nada perceptible.");
        System.out.println();
        System.out.println("  2. Mas aun: con los 23 personajes tal como llegan, MergeSort");
        System.out.println("     puede ser incluso MAS LENTO. Son dos motivos. El primero es");
        System.out.println("     que con n chico las constantes pesan mas que el orden, y");
        System.out.println("     MergeSort reserva memoria para las sublistas en cada nivel de");
        System.out.println("     la recursion. El segundo es que los personajes llegan");
        System.out.println("     agrupados por genero, o sea parcialmente ordenados, que es la");
        System.out.println("     mejor entrada posible para insercion y burbujeo.");
        System.out.println();
        System.out.println("  3. La ventaja de MergeSort aparece cuando n crece, porque n^2 sube");
        System.out.println("     mucho mas rapido que n log n. Se lo elige igual para el lote");
        System.out.println("     inicial porque su Theta(n log n) esta garantizado SIEMPRE, sin");
        System.out.println("     depender de como vengan los datos: es una garantia, no un");
        System.out.println("     promedio. Los cuadraticos son rapidos solo si tienen suerte");
        System.out.println("     con la entrada.");
    }

    // ------------------------------------------------------------------

    /** Imprime la comparacion de los tres algoritmos sobre una lista dada. */
    private static void medirTanda(List<Personaje> datos, int reps) {
        double m = medir(datos, OrdenadorPersonajes::mergeSort, reps);
        double b = medir(datos, OrdenadorPersonajes::burbujeo, reps);
        double i = medir(datos, OrdenadorPersonajes::insercionSimple, reps);

        System.out.printf("%n  %-22s %-16s %s%n", "ALGORITMO", "TIEMPO", "ORDEN");
        System.out.println("  " + "-".repeat(58));
        System.out.printf("  %-22s %-16s %s%n", "MergeSort",        ms(m), "Theta(n log n)");
        System.out.printf("  %-22s %-16s %s%n", "Burbujeo",         ms(b), "O(n^2)");
        System.out.printf("  %-22s %-16s %s%n", "Insercion simple", ms(i), "O(n^2)");
        System.out.printf("%n  Burbujeo tarda %.1f veces lo que MergeSort.%n", b / m);
        System.out.printf("  Diferencia absoluta: %s por ordenamiento.%n", ms(b - m));
    }

    /**
     * Corre el algoritmo 'reps' veces y devuelve el promedio en milisegundos.
     * Antes hace una tanda de calentamiento que no se cuenta.
     */
    private static double medir(List<Personaje> datos,
                                BiFunction<List<Personaje>, Comparator<Personaje>, List<Personaje>> algoritmo,
                                int reps) {

        for (int i = 0; i < Math.min(reps, 1000); i++) {   // calentamiento
            algoritmo.apply(datos, CRITERIO);
        }

        long inicio = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            algoritmo.apply(datos, CRITERIO);
        }
        long fin = System.nanoTime();

        return (fin - inicio) / 1_000_000.0 / reps;   // ms por ejecucion
    }

    /** Formatea un tiempo en ms con la precision necesaria para que se lea. */
    private static String ms(double valor) {
        if (valor >= 1)     return String.format("%.3f ms", valor);
        if (valor >= 0.001) return String.format("%.4f ms", valor);
        return String.format("%.6f ms", valor);
    }

    /** Genera n personajes al azar, solo para medir con entradas grandes. */
    private static List<Personaje> generar(int n) {
        Random r = new Random(42);            // semilla fija: medicion reproducible
        List<Personaje> lista = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            lista.add(new Personaje(i + 1, "P" + (i + 1),
                    Genero.values()[r.nextInt(2)],
                    r.nextBoolean(), r.nextBoolean(),
                    ColorPelo.values()[r.nextInt(3)]));
        }
        Collections.shuffle(lista, r);
        return lista;
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(texto);
        System.out.println("=".repeat(70));
    }
}
