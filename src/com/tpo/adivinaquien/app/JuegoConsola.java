package com.tpo.adivinaquien.app;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.juego.*;
import com.tpo.adivinaquien.jugador.*;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Scanner;

/**
 * VISTA DE CONSOLA. Solo presentacion: lee input, imprime y llama a la Partida.
 * No hay logica de juego aca; el razonamiento vive en modelo, jugador y juego.
 *
 * Es la vista que sirve para la defensa oral: muestra turno por turno que
 * filtros evaluo la maquina, cuanto cortaba cada uno, cual eligio y por que.
 */
public class JuegoConsola {

    private final Scanner scanner = new Scanner(System.in);
    private final RegistroRazonamiento registro = new RegistroConsola();

    public static void main(String[] args) {
        new JuegoConsola().ejecutar();
    }

    public void ejecutar() {
        titulo("ADIVINA QUIEN - TPO Programacion III | Divide and Conquer + Greedy");

        boolean seguir = true;
        while (seguir) {
            System.out.println();
            System.out.println("  1. Jugador vs Maquina");
            System.out.println("  2. Maquina vs Maquina (muestra todo el razonamiento)");
            System.out.println("  3. Simulacion: las 23 partidas con cada estrategia");
            System.out.println("  4. Verificar catalogo e insercion binaria");
            System.out.println("  0. Salir");

            switch (leerEntero("  Opcion: ", 0, 4)) {
                case 1 -> jugadorVsMaquina();
                case 2 -> maquinaVsMaquina();
                case 3 -> SimulacionEstrategias.main(new String[0]);
                case 4 -> VerificacionCatalogo.main(new String[0]);
                default -> seguir = false;
            }
        }
        System.out.println("\nHasta luego.");
    }

    // ================= MODO 1: JUGADOR VS MAQUINA =================

    private void jugadorVsMaquina() {
        titulo("JUGADOR VS MAQUINA");
        CatalogoPersonajes.getInstancia().getOrdenDeCarga().forEach(p -> System.out.println("  " + p));

        Personaje secretoHumano = elegirPersonajeDelHumano();
        JugadorHumano humano = new JugadorHumano("VOS", registro);
        MaquinaGreedy maquina = new MaquinaGreedy("MAQUINA", registro);

        System.out.println("\n  Elegiste a " + secretoHumano.getNombre()
                + ". La maquina ya eligio el suyo.");
        System.out.println("  Criterio de la maquina: " + maquina.getCriterio());

        Partida partida = new Partida(humano, secretoHumano,
                maquina, new SelectorDePersonaje().elegir(), registro);

        while (!partida.haTerminado()) {
            partida.anunciarTurno();
            Jugada jugada = partida.getEnTurno() == humano
                    ? pedirJugadaAlHumano(humano)
                    : maquina.jugarTurno();
            System.out.println("  >> " + partida.aplicarJugada(jugada).mensaje());
        }
        anunciarGanador(partida, humano, maquina);
    }

    private Personaje elegirPersonajeDelHumano() {
        while (true) {
            System.out.print("\n  Elegi tu personaje (nombre o numero): ");
            String entrada = scanner.nextLine().trim();

            Personaje p = entrada.matches("\\d+")
                    ? buscarPorId(Integer.parseInt(entrada))
                    : CatalogoPersonajes.getInstancia().buscarPorNombre(entrada);

            if (p != null) return p;
            System.out.println("  No encontre ese personaje.");
        }
    }

    private Jugada pedirJugadaAlHumano(JugadorHumano humano) {
        while (true) {
            System.out.println("\n  Te quedan " + humano.getCantidadCandidatos() + " candidatos:");
            System.out.println("    " + String.join(", ",
                    humano.getCandidatos().stream().map(Personaje::getNombre).toList()));
            System.out.println("  1. Preguntar   2. Arriesgar   3. Ver sugerencia greedy");

            switch (leerEntero("  Opcion: ", 1, 3)) {
                case 1 -> {
                    List<Filtro> disponibles = humano.filtrosDisponibles();
                    if (disponibles.isEmpty()) {
                        System.out.println("  Ya preguntaste todo, tenes que arriesgar.");
                    } else {
                        return new PreguntaFiltro(disponibles.get(
                                elegirDeLista(disponibles.stream().map(Filtro::getDescripcion).toList(),
                                        "  Pregunta: ")));
                    }
                }
                case 2 -> {
                    List<Personaje> c = humano.getCandidatos();
                    System.out.println("  OJO: si te equivocas, perdes la partida.");
                    return new Suposicion(c.get(elegirDeLista(
                            c.stream().map(Personaje::getNombre).toList(), "  Arriesgo: ")));
                }
                case 3 -> mostrarSugerencias(humano);
            }
        }
    }

    /**
     * Muestra la evaluacion greedy sobre el tablero del humano. No juega por el:
     * le permite comparar su decision con la del algoritmo.
     */
    private void mostrarSugerencias(JugadorHumano humano) {
        System.out.println("\n  Evaluacion greedy sobre tus " + humano.getCantidadCandidatos()
                + " candidatos (menor peor caso = mejor pregunta):");
        List<EvaluacionFiltro> sugerencias = humano.sugerencias();
        for (int i = 0; i < sugerencias.size(); i++) {
            System.out.printf("    %s %s%n", i == 0 ? "->" : "  ", sugerencias.get(i));
        }
    }

    // ================= MODO 2: MAQUINA VS MAQUINA =================

    /**
     * Las dos maquinas juegan entre si mostrando todo el razonamiento. Es lo que
     * pide el enunciado: "que se puedan presenciar todos los procesos realizados
     * por la misma para poder acortar la busqueda de solucion".
     */
    private void maquinaVsMaquina() {
        titulo("MAQUINA VS MAQUINA");

        SelectorDePersonaje selector = new SelectorDePersonaje();
        MaquinaGreedy greedy = new MaquinaGreedy("GREEDY", registro);
        JugadorMaquina secuencial = new MaquinaSecuencial("SECUENCIAL", registro, false);

        System.out.println("  GREEDY: " + greedy.getCriterio());
        System.out.println("  SECUENCIAL: " + secuencial.getCriterio());

        Partida partida = new Partida(greedy, selector.elegir(),
                secuencial, selector.elegir(), registro);

        while (!partida.haTerminado()) {
            partida.anunciarTurno();
            JugadorMaquina actor = (JugadorMaquina) partida.getEnTurno();
            System.out.println("  >> " + partida.aplicarJugada(actor.jugarTurno()).mensaje());
            System.out.print("  [Enter para el proximo turno] ");
            scanner.nextLine();
        }

        anunciarGanador(partida, greedy, secuencial);

        // Contar solo preguntas esconde la diferencia: las dos pueden haber
        // preguntado lo mismo y una haber terminado y la otra no.
        System.out.println("\n  ESTADO FINAL DE CADA MAQUINA");
        System.out.printf("    %-12s %-10s %-12s %s%n", "MAQUINA", "PREGUNTAS", "CANDIDATOS", "ESTADO");
        estadoFinal(greedy);
        estadoFinal(secuencial);
    }

    private void estadoFinal(JugadorMaquina m) {
        int restantes = m.getCantidadCandidatos();
        String estado = restantes == 1
                ? "resolvio (caso base alcanzado)"
                : "le faltaban " + (int) Math.ceil(Math.log(restantes) / Math.log(2)) + " pregunta(s)";
        System.out.printf("    %-12s %-10d %-12d %s%n",
                m.getNombre(), m.getPreguntasHechas(), restantes, estado);
    }

    // ================= AUXILIARES =================

    private void anunciarGanador(Partida partida, Jugador a, Jugador b) {
        System.out.println("\n  ==========================================");
        System.out.println("  GANA: " + partida.getGanador().getNombre());
        System.out.println("  Turnos jugados: " + partida.getNumeroTurno());
        System.out.println("  " + a.getNombre() + " habia elegido a " + partida.getSecretoDe(a).getNombre());
        System.out.println("  " + b.getNombre() + " habia elegido a " + partida.getSecretoDe(b).getNombre());
        System.out.println("  ==========================================");
    }

    /** Imprime una lista numerada y devuelve el indice elegido (base 0). */
    private int elegirDeLista(List<String> opciones, String prompt) {
        System.out.println();
        for (int i = 0; i < opciones.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, opciones.get(i));
        }
        return leerEntero(prompt, 1, opciones.size()) - 1;
    }

    private Personaje buscarPorId(int id) {
        return CatalogoPersonajes.getInstancia().getOrdenDeCarga().stream()
                .filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private int leerEntero(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor >= min && valor <= max) return valor;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Ingresa un numero entre " + min + " y " + max + ".");
        }
    }

    private void titulo(String texto) {
        System.out.println("\n" + "=".repeat(63));
        System.out.println("  " + texto);
        System.out.println("=".repeat(63));
    }
}
