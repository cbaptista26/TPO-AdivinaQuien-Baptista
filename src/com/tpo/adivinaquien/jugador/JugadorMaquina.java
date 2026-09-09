package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.CatalogoFiltros;
import com.tpo.adivinaquien.modelo.Filtro;

import com.tpo.adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base de las maquinas. Lo unico que agrega sobre Jugador es decidir sola que
 * jugar; el Divide and Conquer esta en la clase padre.
 */
public abstract class JugadorMaquina extends Jugador {

    protected JugadorMaquina(String nombre, List<Personaje> candidatosIniciales,
                             RegistroRazonamiento registro) {
        super(nombre, candidatosIniciales, registro);
    }

    /** La maquina si decide sola, asi que siempre devuelve una jugada. */
    @Override
    public Optional<Jugada> decidirJugada() {
        return Optional.of(jugarTurno());
    }

    @Override
    public boolean esMaquina() { return true; }

    /**
     * La funcion de factibilidad del esquema greedy. Saca dos cosas: los filtros
     * que ya pregunte, y los que dejarian un lado vacio.
     *
     * El segundo caso es el interesante. Si ya se que a los 8 candidatos que me
     * quedan no les pregunte el color pero descarte colorado y negro, entonces
     * "tiene el pelo amarillo?" tiene respuesta forzosa: gasto un turno sin
     * descartar a nadie. No lo resolvi con reglas escritas a mano tipo "si ya
     * preguntaste dos colores no preguntes el tercero", sino midiendo la
     * particion real. Asi funciona para cualquier dependencia entre atributos.
     *
     * Midiendo cuanto aporta cada parte del greedy (seccion 5.3), esta funcion
     * resulto ser la que mas impacta.
     */
    protected List<Filtro> filtrosFactibles() {
        List<Filtro> factibles = new ArrayList<>();

        for (Filtro f : CatalogoFiltros.todos()) {
            if (filtrosUsados.contains(f.getClave())) continue;
            if (aplicaFactibilidad() && descomponer(candidatos, f).esInutil()) continue;
            factibles.add(f);
        }
        return factibles;
    }

    /**
     * Si aplica o no la factibilidad. Redefinible para poder medir por separado
     * cuanto aporta cada elemento del greedy (ver SimulacionEstrategias).
     */
    protected boolean aplicaFactibilidad() { return true; }

    /**
     * El turno de la maquina. La estructura es siempre la misma (el esquema
     * D&C); lo unico que cambia entre estrategias es elegirFiltro().
     */
    public Jugada jugarTurno() {

        if (esCasoBase()) {                       // CasoBase(x)
            registro.registrar(String.format(
                    "    [%s] CASO BASE: queda 1 candidato -> lanzo la suposicion.", getNombre()));
            return solucionDirecta();             // SolucionDirecta(x)
        }

        List<Filtro> factibles = filtrosFactibles();

        if (factibles.isEmpty()) {
            registro.registrar(String.format(
                    "    [%s] No quedan filtros que aporten informacion -> arriesgo.", getNombre()));
            return new Suposicion(candidatos.get(0));
        }

        return new PreguntaFiltro(elegirFiltro(factibles));
    }

    /** FUNCION DE SELECCION: el criterio propio de cada estrategia. */
    protected abstract Filtro elegirFiltro(List<Filtro> factibles);
}
