# UML del proyecto

Los diagramas fuente de este directorio están escritos en PlantUML (`.puml`) para que se puedan revisar y versionar junto al código. `domain-class-diagram.puml` describe el código actual; `domain-sequences.puml` transcribe los flujos de dominio planteados en los PDF y señala cuándo un flujo aún no está implementado. La secuencia `updateGame` describe la actualización, los rebotes entre balones y participantes, los goles y el fin de partida.

- `domain-class-diagram.puml`: clases principales del dominio, relaciones y puerto del controlador.
- `start-session-sequence.puml`: flujo implementado de creación reflectiva de una sesión.
- `player-vs-bot-sequence.puml`: inicialización implementada de participantes y lados.
- `by-time-check-sequence.puml`: comprobación implementada del límite de tiempo y muerte súbita.
- `domain-sequences.puml`: secuencias del modelo compartidas con los PDF de referencia.
- `domain-review.md`: contraste entre UML, reglas documentadas y código.
- `package-diagram.puml`: dependencias de alto nivel entre presentación y dominio.
- `player-vs-player-sequence.puml`: inicialización actual de dos jugadores humanos.
- `presentation.testing`: aplicación independiente de banco de pruebas visual para las mecánicas del dominio.
- `mechanics-test-sequence.puml`: flujo del banco visual a través de la fachada `Game`, con instantáneas inmutables y preparación de escenarios.
- `capturas/`: capturas legibles correspondientes a los diagramas cuando se exporten.

Los métodos vacíos no se presentan como comportamientos ya realizados. Las capturas PDF recibidas se conservan en `capturas/`; las páginas vacías se anotan en el informe. Las imágenes PNG de los diagramas PlantUML se regeneran en `capturas/` desde sus fuentes.

### Lectura rápida del modelo

- **Presentación:** muestra pantallas y recoge acciones. El `GameController` es el puente previsto hacia el dominio; la aplicación oficial todavía no integra una partida completa.
- **Banco de pruebas:** `MechanicsTestApp` abre primero `MechanicsTestSetupPanel`, donde se configuran nombres completos de clases para la sesión, la modalidad y los poderes, además de parámetros y perfil de bot. `MechanicsTestConfiguration` conserva esos valores para reiniciar la sesión. El banco acepta extensiones compatibles con los contratos de construcción expuestos por `Game`. `MechanicsTestController`, `MechanicsTestPanel` y `PresentationMovement` solo se comunican con el dominio por medio de `Game` y su instantánea inmutable. El desplazamiento horizontal usa `Game.movePlayer`; el salto se simula en la presentación y su posición participa en las colisiones. El banco permite aterrizar encima del balón.
- **Partida:** `Game` crea y administra sesiones. `GameSession` mantiene cancha, participantes, marcador y regla; coordina movimiento, colisiones entre balones y participantes, goles y fin de partida. Al caminar empuja balones quietos; los balones en movimiento rebotan con los participantes. Al aterrizar encima, el jugador se mantiene sobre el balón y este adquiere velocidad hacia abajo.
- **Cancha y balón:** `SoccerField` contiene y recorre los balones, resuelve colisiones entre ellos como masas iguales y contra participantes como superficies fijas, con restitución de 0.70. En contactos desde arriba mantiene al jugador sobre el balón y lo desplaza hacia abajo. También administra límites y zonas de gol. `Ball` conserva su estado físico y ejecuta el avance o rebote solicitado.
- **Hitboxes:** `Hitbox` define las operaciones comunes; las formas circulares y rectangulares hacen la comprobación geométrica mediante `HitboxIntersections`. `HitboxContact` indica qué entidad y forma hicieron contacto. El paquete detecta; el dominio decide qué hacer después.
- **Modalidad:** `TypeOfMatch` define la consulta de continuidad. `ByGoals` termina al alcanzar el límite; `ByTime` controla el tiempo y conserva el empate en muerte súbita.

Estas descripciones son una guía superficial de lectura. Las reglas detalladas están en `docs/logica-proyecto.md`; el diagrama de clases y las secuencias muestran las relaciones y el orden de las llamadas.
