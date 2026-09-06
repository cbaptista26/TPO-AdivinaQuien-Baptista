package com.tpo.adivinaquien.app;

import com.tpo.adivinaquien.catalogo.CatalogoPersonajes;
import com.tpo.adivinaquien.juego.ModoJuego;
import com.tpo.adivinaquien.juego.Partida;
import com.tpo.adivinaquien.juego.RegistroConsola;
import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.juego.ResultadoTurno;
import com.tpo.adivinaquien.juego.SelectorDePersonaje;
import com.tpo.adivinaquien.jugador.Jugada;
import com.tpo.adivinaquien.jugador.JugadorHumano;
import com.tpo.adivinaquien.jugador.JugadorMaquina;
import com.tpo.adivinaquien.jugador.MaquinaGreedy;
import com.tpo.adivinaquien.jugador.MaquinaSecuencial;
import com.tpo.adivinaquien.jugador.PreguntaFiltro;
import com.tpo.adivinaquien.jugador.Suposicion;
import com.tpo.adivinaquien.modelo.EvaluacionFiltro;
import com.tpo.adivinaquien.modelo.Filtro;
import com.tpo.adivinaquien.modelo.Personaje;

import java.util.List;
import java.util.Scanner;

/**
 * ===========================================================================
 * LA VISTA DE CONSOLA
 * ===========================================================================
 *
 * Esta clase es UNICAMENTE presentacion: lee input, imprime y llama a la
 * Partida. No hay ni una linea de logica de juego aca adentro. Todo el
 * razonamiento algoritmico vive en los paquetes modelo, jugador y juego.
 *
 * Es la vista que sirve para la defensa oral: muestra, turno por turno, que
 * filtros evaluo la maquina, cuanto cortaba cada uno, cual eligio y por que.
 * Si se cierra esta clase, el juego sigue funcionando con la vista Swing: solo
 * cambia quien recibe los mensajes del RegistroRazonamiento.
 */
public class JuegoConsola {

    private final Scanner scanner = new Scanner(System.in);
    private final RegistroRazonamiento registro = new RegistroConsola();

    public static void main(String[] args) {
        new JuegoConsola().ejecutar();
    }

    public void ejecutar() {
        System.out.println();
        System.out.println("===============================================================");
        System.out.println("  ADIVINA QUIEN - TPO Programacion III");
        System.out.println("  Divide and Conquer + Greedy");
        System.out.println("===============================================================");

        boolean seguir = true;
        while (seguir) {
            switch (menuPrincipal()) {
                case 1 -> jugadorVsMaquina();
                case 2 -> maquinaVsMaquina();
                case 3 -> SimulacionEstrategias.main(new String[0]);
                case 4 -> VerificacionCatalogo.main(new String[0]);
                default -> seguir = false;
            }
        }
        System.out.println("\nHasta luego.");
    }

    // ==================================================================
    // MENU
    // ==================================================================

    private int menuPrincipal() {
        System.out.println();
        System.out.println("---------------------------------------------------------------");
        for (ModoJuego m : ModoJuego.values()) {
            System.out.printf("  %d. %-24s %s%n",
                    m.ordinal() + 1, m.getTitulo(), m.getDescripcion());
        }
        System.out.println("  4. Verificar catalogo      Muestra la carga y la insercion binaria.");
        System.out.println("  0. Salir");
        System.out.println("---------------------------------------------------------------");
        return leerEntero("Opcion: ", 0, 4);
    }

    // ==================================================================
    // MODO 1: JUGADOR VS MAQUINA
    // ==================================================================

    private void jugadorVsMaquina() {
        titulo("JUGADOR VS MAQUINA");

        mostrarTablero(CatalogoPersonajes.getInstancia().getOrdenDeCarga());

        Personaje secretoHumano = elegirPersonajeDelHumano();
        Personaje secretoMaquina = new SelectorDePersonaje().elegir();

        JugadorHumano humano = new JugadorHumano("VOS", registro);
        MaquinaGreedy maquina = new MaquinaGreedy("MAQUINA", registro);

        System.out.println();
        System.out.println("  Elegiste a " + secretoHumano.getNombre()
                + ". La maquina ya eligio el suyo (no te lo va a decir).");
        System.out.println("  Criterio de la maquina: " + maquina.getCriterio());

        Partida partida = new Partida(humano, secretoHumano, maquina, secretoMaquina, registro);

        while (!partida.haTerminado()) {
            partida.anunciarTurno();
            Jugada jugada = partida.getEnTurno() == humano
                    ? pedirJugadaAlHumano(humano)
                    : maquina.jugarTurno();

            ResultadoTurno r = partida.aplicarJugada(jugada);
            System.out.println("  >> " + r.mensaje());
        }

        anunciarGanador(partida, humano, maquina);
    }

    /** Le pide al humano que elija su personaje secreto. */
    private Personaje elegirPersonajeDelHumano() {
        while (true) {
            System.out.print("\n  Elegi tu personaje (nombre o numero): ");
            String entrada = scanner.nextLine().trim();

            Personaje p = entrada.matches("\\d+")
                    ? buscarPorId(Integer.parseInt(entrada))
                    : CatalogoPersonajes.getInstancia().buscarPorNombre(entrada);

            if (p != null) return p;
            System.out.println("  No encontre ese personaje. Proba de nuevo.");
        }
    }

    /** Menu del turno del humano: preguntar, arriesgar o pedir una sugerencia. */
    private Jugada pedirJugadaAlHumano(JugadorHumano humano) {
        while (true) {
            System.out.println();
            System.out.println("  Te quedan " + humano.getCantidadCandidatos() + " candidatos:");
            System.out.println("    " + nombresDe(humano.getCandidatos()));
            System.out.println("  1. Preguntar por una caracteristica");
            System.out.println("  2. Arriesgar quien es");
            System.out.println("  3. Ver que preguntaria la maquina (sugerencia greedy)");

            switch (leerEntero("  Opcion: ", 1, 3)) {
                case 1 -> {
                    Filtro f = elegirFiltro(humano);
                    if (f != null) return new PreguntaFiltro(f);
                }
                case 2 -> {
                    Personaje p = elegirCandidato(humano.getCandidatos());
                    if (p != null) return new Suposicion(p);
                }
                case 3 -> mostrarSugerencias(humano);
            }
        }
    }

    private Filtro elegirFiltro(JugadorHumano humano) {
        List<Filtro> disponibles = humano.filtrosDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("  Ya preguntaste todo. Tenes que arriesgar.");
            return null;
        }
        System.out.println();
        for (int i = 0; i < disponibles.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, disponibles.get(i).getDescripcion());
        }
        int op = leerEntero("  Pregunta: ", 1, disponibles.size());
        return disponibles.get(op - 1);
    }

    private Personaje elegirCandidato(List<Personaje> candidatos) {
        System.out.println();
        for (int i = 0; i < candidatos.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, candidatos.get(i).getNombre());
        }
        System.out.println("  OJO: si te equivocas, perdes la partida.");
        int op = leerEntero("  Arriesgo: ", 1, candidatos.size());
        return candidatos.get(op - 1);
    }

    /**
     * Muestra la evaluacion greedy sobre el tablero del humano.
     *
     * No juega por el: solo le muestra el mismo calculo que hace la maquina,
     * para que se pueda comparar la decision humana con la del algoritmo.
     */
    private void mostrarSugerencias(JugadorHumano humano) {
        System.out.println();
        System.out.println("  Evaluacion greedy sobre tus " + humano.getCantidadCandidatos()
                + " candidatos (menor peor caso = mejor pregunta):");
        List<EvaluacionFiltro> sugerencias = humano.sugerencias();
        for (int i = 0; i < sugerencias.size(); i++) {
            System.out.printf("    %s %s%n",
                    i == 0 ? "->" : "  ", sugerencias.get(i));
        }
    }

    // ==================================================================
    // MODO 2: MAQUINA VS MAQUINA
    // ==================================================================

    /**
     * Las dos maquinas juegan entre si mostrando todo el razonamiento.
     *
     * Es el modo que pide el enunciado: "que se puedan presenciar todos los
     * procesos realizados por la misma para poder acortar la busqueda de
     * solucion". Cada turno imprime que filtros evaluo la maquina, cuanto
     * cortaba cada uno, cual eligio, por que, y en cuanto quedo el conjunto.
     */
    private void maquinaVsMaquina() {
        titulo("MAQUINA VS MAQUINA");

        SelectorDePersonaje selector = new SelectorDePersonaje();
        Personaje secretoA = selector.elegir();
        Personaje secretoB = selector.elegir();

        MaquinaGreedy greedy = new MaquinaGreedy("GREEDY", registro);
        JugadorMaquina secuencial = new MaquinaSecuencial("SECUENCIAL", registro, false);

        System.out.println("  " + greedy.getNombre() + ": " + greedy.getCriterio());
        System.out.println("  " + secuencial.getNombre() + ": " + secuencial.getCriterio());
        System.out.println();
        System.out.println("  Empieza GREEDY. Los personajes secretos se revelan al final.");

        Partida partida = new Partida(greedy, secretoA, secuencial, secretoB, registro);

        while (!partida.haTerminado()) {
            partida.anunciarTurno();
            JugadorMaquina actor = (JugadorMaquina) partida.getEnTurno();
            ResultadoTurno r = partida.aplicarJugada(actor.jugarTurno());
            System.out.println("  >> " + r.mensaje());
            pausar();
        }

        anunciarGanador(partida, greedy, secuencial);

        // Contar solo preguntas esconde la diferencia: las dos maquinas pueden
        // haber preguntado lo mismo y sin embargo una haber terminado y la otra no.
        // Lo que importa es cuanto le falta a cada una para llegar al caso base.
        System.out.println();
        System.out.println("  ESTADO FINAL DE CADA MAQUINA");
        System.out.printf("    %-12s %-10s %-14s %s%n",
                "MAQUINA", "PREGUNTAS", "CANDIDATOS", "ESTADO");
        System.out.println("    " + "-".repeat(52));
        estadoFinal(greedy);
        estadoFinal(secuencial);
    }

    // ==================================================================
    // AUXILIARES DE PRESENTACION
    // ==================================================================

    private void anunciarGanador(Partida partida,
                                 com.tpo.adivinaquien.jugador.Jugador a,
                                 com.tpo.adivinaquien.jugador.Jugador b) {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("  GANA: " + partida.getGanador().getNombre());
        System.out.println("  Turnos jugados: " + partida.getNumeroTurno());
        System.out.println("  " + a.getNombre() + " habia elegido a "
                + partida.getSecretoDe(a).getNombre()
                + " (lo tenia que adivinar " + b.getNombre() + ")");
        System.out.println("  " + b.getNombre() + " habia elegido a "
                + partida.getSecretoDe(b).getNombre()
                + " (lo tenia que adivinar " + a.getNombre() + ")");
        System.out.println("  ==========================================");
    }

    /** Una fila del resumen final: cuanto le falta a esta maquina para resolver. */
    private void estadoFinal(JugadorMaquina m) {
        int restantes = m.getCantidadCandidatos();
        String estado = restantes == 1
                ? "resolvio (caso base alcanzado)"
                : "le faltaban " + (int) Math.ceil(Math.log(restantes) / Math.log(2))
                + " pregunta(s) mas";
        System.out.printf("    %-12s %-10d %-14d %s%n",
                m.getNombre(), m.getPreguntasHechas(), restantes, estado);
    }

    private void mostrarTablero(List<Personaje> personajes) {
        System.out.println();
        System.out.println("  TABLERO (" + personajes.size() + " personajes):");
        personajes.forEach(p -> System.out.println("  " + p));
    }

    private String nombresDe(List<Personaje> personajes) {
        return String.join(", ", personajes.stream().map(Personaje::getNombre).toList());
    }

    private Personaje buscarPorId(int id) {
        return CatalogoPersonajes.getInstancia().getOrdenDeCarga().stream()
                .filter(p -> p.getId() == id)
                .findFirst().orElse(null);
    }

    private int leerEntero(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String linea = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor >= min && valor <= max) return valor;
            } catch (NumberFormatException ignored) {
                // se vuelve a pedir
            }
            System.out.println("  Ingresa un numero entre " + min + " y " + max + ".");
        }
    }

    private void pausar() {
        System.out.print("  [Enter para el proximo turno] ");
        scanner.nextLine();
    }

    private void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(63));
        System.out.println("  " + texto);
        System.out.println("=".repeat(63));
    }
}