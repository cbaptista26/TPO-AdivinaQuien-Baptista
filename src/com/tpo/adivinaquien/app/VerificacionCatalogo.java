package com.tpo.adivinaquien.app;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.modelo.CatalogoFiltros;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Programa de verificacion del modelo y del catalogo.
 *
 * No es parte del juego: sirve para comprobar que las decisiones de disenio
 * se cumplen de verdad y para tener numeros concretos.
 */
public class VerificacionCatalogo {

    public static void main(String[] args) {
        CatalogoPersonajes catalogo = CatalogoPersonajes.getInstancia();

        titulo("1. ORDEN DE CARGA (agrupados solo por genero)");
        catalogo.getOrdenDeCarga().forEach(System.out::println);

        titulo("2. TRAZA DE LA INSERCION BINARIA");
        System.out.println("  Cada alta busca su posicion dividiendo el rango a la mitad:");
        catalogo.getTrazaDeCarga().forEach(System.out::println);

        titulo("3. LISTA ORDENADA QUE ARMO LA MAQUINA");
        catalogo.getOrdenados().forEach(System.out::println);

        titulo("4. VERIFICACION DE UNICIDAD");
        Set<String> claves = new HashSet<>();
        catalogo.getOrdenDeCarga().forEach(p -> claves.add(p.claveAtributos()));
        System.out.println("  Personajes cargados       : " + catalogo.getCantidad());
        System.out.println("  Combinaciones distintas   : " + claves.size());
        System.out.println("  Todos unicos              : "
                + (claves.size() == catalogo.getCantidad() ? "SI" : "NO"));

        titulo("5. PODER DE CORTE DE CADA FILTRO EN EL TURNO 1");
        List<Personaje> todos = catalogo.getOrdenDeCarga();
        for (Filtro f : CatalogoFiltros.todos()) {
            int cumplen = 0;
            for (Personaje p : todos) {
                if (f.evaluar(p)) cumplen++;
            }
            int noCumplen = todos.size() - cumplen;
            System.out.printf("  %-26s parte %2d / %2d  -> peor caso: %2d%n",
                    f.getDescripcion(), cumplen, noCumplen, Math.max(cumplen, noCumplen));
        }

        titulo("6. COTA TEORICA");
        int n = catalogo.getCantidad();
        int minimo = (int) Math.ceil(Math.log(n) / Math.log(2));
        System.out.println("  Buscar uno por uno (fuerza bruta) : hasta " + n + " intentos  -> O(n)");
        System.out.println("  Partiendo el conjunto a la mitad  : " + minimo
                + " preguntas + 1 suposicion  -> O(log n)");
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(texto);
        System.out.println("=".repeat(70));
    }
}
