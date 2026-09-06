package com.tpo.adivinaquien.jugador;

import com.tpo.adivinaquien.juego.RegistroRazonamiento;
import com.tpo.adivinaquien.modelo.CatalogoFiltros;
import com.tpo.adivinaquien.modelo.Filtro;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de las maquinas. Lo unico que agrega sobre Jugador es decidir sola que
 * jugar; el Divide and Conquer esta en la clase padre.
 */
public abstract class JugadorMaquina extends Jugador {

    protected JugadorMaquina(String nombre, RegistroRazonamiento registro) {
        super(nombre, registro);
    }

    @Override
    public boolean esMaquina() { return true; }

    /**
     * FUNCION DE FACTIBILIDAD del esquema greedy. Descarta los filtros ya
     * preguntados y los que dejarian un lado vacio (su respuesta ya se conoce,
     * asi que gastarian un turno sin descartar a nadie).
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
