# ⚽ Head Soccer

<div align="center">

### Proyecto universitario · Escuela Colombiana de Ingeniería Julio Garavito

*Un videojuego de fútbol 1 vs 1 con cabezones, desarrollado en Java.*

[![Compilación](https://github.com/PaglkG/ProyectoFinal-Dopo/actions/workflows/build.yml/badge.svg)](https://github.com/PaglkG/ProyectoFinal-Dopo/actions/workflows/build.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-compilación-C71A36?logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)
![Licencia](https://img.shields.io/badge/uso-acad%C3%A9mico-blue)

</div>

---

## 📖 Sobre el proyecto

Este repositorio contiene el **proyecto final** de la asignatura, desarrollado en la **Escuela Colombiana de Ingeniería Julio Garavito**, como parte del programa de **Ingeniería de Sistemas**.

Es un trabajo **académico y sin fines comerciales**. Su objetivo es aplicar análisis de requisitos, diseño orientado a objetos, pruebas automáticas y trabajo colaborativo con Git.

El juego que implementamos es una versión propia inspirada en **Head Soccer**, el juego ganador de la convocatoria *Juego 2026-2*.

> ⚠️ **Aviso:** *Head Soccer* es un juego creado por **D&D Dream Corp.** Este proyecto es una reinterpretación educativa; no está afiliado ni respaldado por sus creadores y no se distribuye con fines comerciales.

## 📂 Documentación y trabajo colaborativo

Aquí se encuentran los documentos y recursos compartidos para que todos los integrantes puedan consultar, editar y mantener actualizada la información del proyecto.

| Recurso | Enlace | Descripción |
|---|---|---|
| 📝 Documento principal | [Abrir Google Docs](https://docs.google.com/document/d/1i9cOyOaORip1lEBSSzXO6fPmAldYHS1KjRxTYh8H3WA/edit?usp=sharing) | Documentación escrita y trabajo colaborativo. |
| 📐 Diagrama de clases | [Abrir en diagrams.net](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&link-icons=1&tooltip-icons=1&nav=1&title=Head_Soccer.drawio&dark=auto#Uhttps%3A%2F%2Fdrive.google.com%2Fuc%3Fid%3D1kuQxyHiroSopLOlZwFoC23GFreDoKCrz%26export%3Ddownload) | Visualización y edición del diagrama de clases. |
| 💻 Código fuente | [Repositorio GitHub](https://github.com/PaglkG/ProyectoFinal-Dopo) | Código, versiones, ramas y seguimiento del desarrollo. |

> **Importante:** Los integrantes deben contar con permisos de edición en Google Docs y en el archivo de diagrams.net almacenado en Google Drive. Se recomienda trabajar sobre los documentos originales para evitar duplicados y mantener una única versión actualizada.

## 🎮 ¿De qué trata Head Soccer?

*Head Soccer* es un juego de fútbol arcade, **uno contra uno**, donde los jugadores son personajes de cabeza enorme que golpean el balón con la cabeza y los pies.

Fue lanzado por D&D Dream Corp. en 2012 para dispositivos móviles. Sus desarrolladores reportan más de 100 millones de descargas.

La idea es simple y se aprende en segundos:

- 🥅 **Gana quien marque más goles** antes de que se acabe el tiempo del partido.
- 🏃 Te mueves de lado a lado, **saltas** para cabecear y **pateas** para disparar.
- 💥 Puedes ejecutar **tiros especiales**, por ejemplo, de fuego, hielo o rayo.
- 🧠 Si vas arriba en el marcador, defender bien hasta el final también es una estrategia.
- ⚙️ Utiliza **física** para el balón y los rebotes, haciendo que cada partido sea impredecible.

### Elementos característicos del juego original

| Elemento | Descripción |
|---|---|
| 🧑‍🎤 **Personajes** | Decenas de avatares, cada uno con su propio tiro especial. |
| 🏆 **Modos de juego** | Arcade, Torneo, Supervivencia, Liga, Copa y otros modos. |
| 🌍 **Multijugador** | Partidas contra amigos o jugadores en línea. |
| 🐾 **Personalización** | Sistema de mascotas y accesorios para los personajes. |
| 🎯 **Controles** | Mínimos: moverse, saltar y patear. |

> Las versiones web del juego se controlan con las **flechas del teclado** para moverse y la **barra espaciadora** para saltar.

## 🕹️ Características de nuestra versión

> 🚧 *Esta sección se irá completando a medida que se definan e implementen los requisitos del proyecto.*

- [ ] Partida 1 vs 1 con temporizador y marcador.
- [ ] Movimiento, salto y golpe del balón.
- [ ] Física del balón y colisiones.
- [ ] Selección de personajes.
- [ ] Tiros especiales.
- [ ] Modo contra la máquina.
- [ ] Modo dos jugadores en el mismo equipo.
- [ ] Pruebas unitarias con JUnit 5.

## 🧰 Tecnologías

| Herramienta | Uso |
|---|---|
| ☕ **Java 21** | Lenguaje principal. |
| 📦 **Maven** | Gestión de dependencias y compilación. |
| 🧪 **JUnit 5** | Pruebas unitarias. |
| 🤖 **GitHub Actions** | Integración continua (`build-and-test`). |
| 🔀 **Git / GitHub** | Control de versiones y revisión de código. |

## 🚀 Cómo ejecutarlo

### Requisitos

- JDK 21.
- Maven 3.9 o superior.
- Git.

### Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/PaglkG/ProyectoFinal-Dopo.git

# 2. Entrar al directorio
cd ProyectoFinal-Dopo

# 3. Compilar el proyecto y ejecutar las pruebas
mvn clean verify
```

> La instrucción para lanzar el videojuego se añadirá cuando esté implementada la clase principal.

## 🌿 Flujo de trabajo

Trabajamos con ramas protegidas y revisión cruzada entre integrantes para mantener un desarrollo organizado y evitar que cambios incompletos lleguen a la versión principal.

### Estructura de ramas

```mermaid
gitGraph
    commit id: "inicio"
    branch develop
    checkout develop
    branch pablo
    branch samuel
    checkout pablo
    commit id: "feature Pablo"
    checkout develop
    merge pablo id: "PR + revisión"
    checkout samuel
    commit id: "feature Samuel"
    checkout develop
    merge samuel id: "PR + revisión"
    checkout main
    merge develop id: "entrega"
```

| Rama | Propósito | Reglas |
|---|---|---|
| `main` | Versiones estables, listas para entregar. | Pull Request, 1 aprobación y CI obligatoria. |
| `develop` | Integración del trabajo del equipo. | Pull Request, 1 aprobación y CI obligatoria. |
| `pablo` | Rama personal de desarrollo. | Sin borrado ni *force push*. |
| `samuel` | Rama personal de desarrollo. | Sin borrado ni *force push*. |

### Reglas de colaboración

1. Cada integrante desarrolla sus funcionalidades en su propia rama.
2. Los cambios se envían mediante Pull Requests.
3. El otro integrante revisa el código antes de aprobarlo.
4. Los Pull Requests deben superar las pruebas automáticas de GitHub Actions.
5. Los cambios se integran mediante **Merge Commit**.
6. La rama `main` debe contener únicamente versiones estables del proyecto.

## 👥 Equipo de desarrollo

| Integrante | GitHub | Responsabilidad |
|---|---|---|
| Pablo Andres Gualdron Lindo | [@PaglkG](https://github.com/PaglkG) | Desarrollo y coordinación. |
| Samuel Ahumada Florez | [@usuario-samuel](https://github.com/) | Desarrollo y colaboración. |

## 🎓 Contexto académico

- **Institución:** Escuela Colombiana de Ingeniería Julio Garavito.
- **Programa:** Ingeniería de Sistemas.
- **Tipo de trabajo:** Proyecto final universitario.
- **Juego asignado:** Head Soccer.
- **Periodo:** 2026-2.

## 📚 Créditos y referencias

- *Head Soccer* © **D&D Dream Corp.**, juego original que inspira este proyecto.
- Convocatoria *Juego 2026-2*, Escuela Colombiana de Ingeniería Julio Garavito.

---

<div align="center">

**Hecho con ☕ y mucho código en Bogotá, Colombia 🇨🇴**

*Proyecto académico · Ingeniería de Sistemas · 2026-2*

</div>