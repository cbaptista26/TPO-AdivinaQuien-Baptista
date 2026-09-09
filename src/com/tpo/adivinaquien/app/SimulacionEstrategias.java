package com.tpo.adivinaquien.app;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.juego.Oraculo;
import com.tpo.adivinaquien.juego.OraculoPersonaje;
import com.tpo.adivinaquien.juego.RegistroConsola;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.juego.RegistroSilencioso;
import com.tpo.adivinaquien.jugador.BuscadorRecursivo;
import com.tpo.adivinaquien.jugador.JugadorMaquina;
import com.tpo.adivinaquien.jugador.MaquinaGreedy;
import com.tpo.adivinaquien.jugador.MaquinaSecuencial;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Compara las dos estrategias sobre las 23 partidas posibles.
 *
 * Esto es lo que respalda con numeros la afirmacion "greedy es mejor" en la
 * documentacion. No es una opinion: se prueban todos los personajes secretos
 * posibles con cada estrategia y se cuentan las preguntas.
 */
public class SimulacionEstrategias {

    public static void main(String[] args) {

        List<Personaje> todos = CatalogoPersonajes.getInstancia().getOrdenDeCarga();

        // ---------- 1. Una partida detallada, para ver el razonamiento ----------
        titulo("PARTIDA DE EJEMPLO CON TRAZA COMPLETA (secreto: Profesor Hadrian)");
        RegistroRazonamiento consola = new RegistroConsola();
        Personaje secreto = CatalogoPersonajes.getInstancia().buscarPorNombre("Profesor Hadrian");
        Oraculo oraculo = new OraculoPersonaje(secreto);

        MaquinaGreedy greedy = new MaquinaGreedy("GREEDY", consola);
        BuscadorRecursivo buscador = new BuscadorRecursivo(greedy, consola);
        Personaje encontrado = buscador.resolver(oraculo);

        System.out.println();
        System.out.println("  Resultado: " + encontrado.getNombre()
                + "  |  correcto: " + (oraculo.esElPersonaje(encontrado) ? "SI" : "NO")
                + "  |  preguntas: " + buscador.getPreguntas());

        // ---------- 2. Las 23 partidas con cada estrategia ----------
        titulo("COMPARACION SOBRE LAS 23 PARTIDAS POSIBLES");

        Resultado rGreedy = simular(todos,
                (n, r) -> new MaquinaGreedy(n, r), "GREEDY");

        Resultado rSecFact = simular(todos,
                (n, r) -> new MaquinaSecuencial(n, r, true), "SEC+FACT");

        Resultado rSecPura = simular(todos,
                (n, r) -> new MaquinaSecuencial(n, r, false), "SEC-PURA");

        System.out.printf("  %-34s %-8s %-8s %-10s %s%n",
                "ESTRATEGIA", "PEOR", "MEJOR", "PROMEDIO", "ACIERTOS");
        System.out.println("  " + "-".repeat(72));
        imprimir("Greedy (seleccion + factibilidad)", rGreedy);
        imprimir("Secuencial CON factibilidad", rSecFact);
        imprimir("Secuencial pura (sin greedy)", rSecPura);

        System.out.println();
        System.out.println("  QUE APORTA CADA ELEMENTO DEL ESQUEMA GREEDY");
        System.out.printf("    Factibilidad sola  : %+.2f turnos de promedio (%d -> %d en el peor caso)%n",
                rSecFact.promedio() - rSecPura.promedio(), rSecPura.peor, rSecFact.peor);
        System.out.printf("    Seleccion minimax  : %+.2f turnos de promedio (%d -> %d en el peor caso)%n",
                rGreedy.promedio() - rSecFact.promedio(), rSecFact.peor, rGreedy.peor);
        System.out.printf("    Greedy completo    : %+.2f turnos de promedio (%d -> %d en el peor caso)%n",
                rGreedy.promedio() - rSecPura.promedio(), rSecPura.peor, rGreedy.peor);

        // ---------- 3. Cota teorica ----------
        titulo("CONTRASTE CONTRA LA COTA TEORICA");
        int n = todos.size();
        int cota = (int) Math.ceil(Math.log(n) / Math.log(2));
        System.out.println("  Personajes (n)                        : " + n);
        System.out.println("  Cota inferior teorica techo(log2 n)    : " + cota + " preguntas");
        System.out.println("  Preguntas de greedy en el peor caso    : " + (rGreedy.peor - 1));
        System.out.println("  Turnos totales (preguntas + 1 suposicion): " + rGreedy.peor);
        System.out.println();
        System.out.println("  Greedy alcanza la cota inferior: ninguna estrategia puede");
        System.out.println("  identificar a uno entre " + n + " personajes con menos preguntas,");
        System.out.println("  porque cada pregunta binaria aporta como maximo 1 bit.");
        System.out.println();
        System.out.println("  Fuerza bruta (probar uno por uno)      : hasta " + n
                + " intentos, O(n)");
        System.out.println("  Divide and Conquer                     : " + (rGreedy.peor - 1)
                + " preguntas, O(log n)");
    }

    // ------------------------------------------------------------------

    /** Corre una partida por cada personaje secreto posible y junta estadisticas. */
    private static Resultado simular(List<Personaje> todos,
                                     BiFunction<String, RegistroRazonamiento, JugadorMaquina> fabrica,
                                     String nombre) {

        RegistroRazonamiento mudo = new RegistroSilencioso();
        Resultado r = new Resultado();

        for (Personaje secreto : todos) {
            Oraculo oraculo = new OraculoPersonaje(secreto);
            JugadorMaquina maquina = fabrica.apply(nombre, mudo);
            BuscadorRecursivo buscador = new BuscadorRecursivo(maquina, mudo);

            Personaje encontrado = buscador.resolver(oraculo);

            int turnos = buscador.getPreguntas() + 1;   // +1 por la suposicion final
            r.registrar(turnos, oraculo.esElPersonaje(encontrado));
        }
        return r;
    }

    private static void imprimir(String nombre, Resultado r) {
        System.out.printf("  %-34s %-8d %-8d %-10.2f %d/%d%n",
                nombre, r.peor, r.mejor, r.promedio(), r.aciertos, r.partidas);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(texto);
        System.out.println("=".repeat(70));
    }

    /** Acumulador de estadisticas de una tanda de partidas. */
    private static class Resultado {
        int partidas = 0;
        int aciertos = 0;
        int total = 0;
        int peor = Integer.MIN_VALUE;
        int mejor = Integer.MAX_VALUE;

        void registrar(int turnos, boolean acerto) {
            partidas++;
            if (acerto) aciertos++;
            total += turnos;
            peor = Math.max(peor, turnos);
            mejor = Math.min(mejor, turnos);
        }

        double promedio() {
            return partidas == 0 ? 0 : (double) total / partidas;
        }
    }
}
