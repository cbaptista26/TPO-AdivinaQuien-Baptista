# 🧙 Adivina Quién — Lógica Algorítmica

Implementación en **Java** del clásico juego *Adivina Quién*, ambientado en una **academia de magia**. El proyecto fue desarrollado como **Trabajo Práctico Obligatorio (TPO) para la asignatura Diseño y Análisis de Algoritmos**.

El objetivo principal es aplicar y demostrar conceptos de **Lógica Algorítmica**, utilizando estrategias de **Divide y Conquista** y **Greedy**, junto con diferentes estructuras de datos y análisis de complejidad.

---

## 🧠 Patrones Algorítmicos Aplicados

### Divide y Conquista

* **MergeSort:** utilizado para ordenar inicialmente el catálogo de 23 personajes por sus atributos, con complejidad **Θ(n log n)**.
* **Búsqueda Binaria:** utilizada para encontrar la posición de nuevos personajes dentro del catálogo ordenado, con complejidad **Θ(log n)**.
* **Reducción de candidatos:** en cada turno se divide el conjunto de personajes entre quienes cumplen y no cumplen el filtro seleccionado, conservando únicamente el grupo correspondiente a la respuesta.

### Greedy

La máquina utiliza una estrategia **voraz con criterio minimax** para seleccionar la siguiente pregunta.

En cada turno evalúa los filtros disponibles y elige aquel cuyo **peor caso deje la menor cantidad de candidatos**, buscando una división lo más cercana posible al 50/50.

También se implementa una **función de factibilidad**, que descarta preguntas ya utilizadas o que no permiten reducir el conjunto de candidatos.

---

## 🏗️ Arquitectura

El proyecto separa el **motor lógico** de las diferentes formas de presentación.

* 🎮 **Motor del juego:** administra partidas, jugadores, candidatos, filtros y reglas.
* 🧠 **Máquina Greedy:** selecciona preguntas mediante el criterio minimax.
* 🔎 **Máquina Secuencial:** utiliza un orden fijo de preguntas para comparar estrategias.
* 📝 **Registro de razonamiento:** permite mostrar las decisiones internas de la máquina sin acoplar la lógica a una interfaz específica.
* 🖥️ **Interfaz gráfica:** desarrollada con **Java Swing**.
* 💻 **Consola:** permite jugar y visualizar el razonamiento algorítmico paso a paso.
* 🔇 **Modo silencioso:** utilizado para realizar simulaciones y benchmarks.

---

## 🚀 Modos de Ejecución

El proyecto cuenta con diferentes puntos de entrada:

1. **JuegoConsola**
   Partida interactiva desde la terminal con información detallada sobre las decisiones de la máquina.

2. **VentanaJuego**
   Interfaz gráfica en Swing ambientada en la Academia Umbraluz.

3. **SimulacionEstrategias**
   Ejecuta partidas entre diferentes estrategias para comparar su rendimiento.

4. **VerificacionCatalogo**
   Comprueba las condiciones y restricciones del catálogo antes de iniciar el juego.

5. **BenchmarkOrdenamiento**
   Compara experimentalmente los tiempos de **MergeSort, Burbujeo e Inserción Simple** sobre diferentes tamaños de entrada.

---

## 📋 Juego y Entidades

* 🧙 **23 personajes** pertenecientes a la Academia Umbraluz.
* 🧩 Cada personaje posee una combinación única de atributos.
* 🔮 **6 filtros de búsqueda:**

  * Género
  * Calvicie
  * Anteojos
  * Pelo colorado
  * Pelo negro
  * Pelo amarillo
* 🎯 Las preguntas permiten reducir progresivamente el conjunto de candidatos.
* 🔐 El personaje secreto se obtiene mediante un **Oráculo**, evitando que la lógica del juego pueda acceder directamente al secreto.
* 🏁 La máquina realiza su suposición cuando alcanza un único candidato.

---

## 📊 Complejidad

El análisis del proyecto utiliza:

* **Ordenamiento inicial:** Θ(n log n)
* **Búsqueda binaria:** Θ(log n)
* **Filtrado de candidatos:** Θ(n)
* **Selección Greedy:** Θ(f · n)
* **Partida completa:** Θ(f · n)
* **Aplicación completa:** **Θ(n log n)**

Donde:

* `n` = cantidad de personajes.
* `f` = cantidad de filtros.

La complejidad general queda dominada por el **ordenamiento inicial mediante MergeSort**.

---

## 📈 Comparación de Estrategias

La implementación permite comparar el comportamiento de diferentes estrategias sobre las mismas 23 partidas:

| Estrategia                | Peor caso | Promedio | Aciertos |
| ------------------------- | --------: | -------: | -------: |
| Greedy + Factibilidad     |  6 turnos |     5,61 |    23/23 |
| Secuencial + Factibilidad |  6 turnos |     5,61 |    23/23 |
| Secuencial pura           |  7 turnos |     6,96 |    23/23 |

La experimentación permite observar que, para este catálogo, la **función de factibilidad tiene mayor impacto práctico que la selección minimax**, debido a las dependencias existentes entre los atributos de los personajes.

---

## 🛠️ Tecnologías

* **Java 21**
* **Java Swing**
* **ArrayList**
* **HashSet**
* **HashMap**
* Programación orientada a objetos
* Algoritmos de ordenamiento y búsqueda
* Análisis de complejidad asintótica

---

## 🎓 Asignatura

**Diseño y Análisis de Algoritmos**
Docente: **López Juan Ignacio**

**Alumna:** Candela Baptista

---

*Desarrollado por Candela Baptista.*
