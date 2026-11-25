# UML del proyecto

Las fuentes PlantUML y las capturas se conservan para contrastar el diseño recibido con el código. La reorganización del dominio aplanó todos sus paquetes salvo domain.

- domain-class-diagram.puml y domain-sequences.puml representan el modelo de referencia recibido, no el inventario vigente del código. Sus diferencias conocidas se enumeran en domain-review.md.
- package-diagram.puml y package-diagram.png son anteriores al aplanado del dominio y no representan los paquetes actuales.
- Las secuencias start-session, player-vs-bot, player-vs-player y by-time-check describen flujos implementados, pero se deben contrastar con el código y la lógica documentada antes de tratarlas como contratos.
- mechanics-test-sequence.puml representa el banco visual extensible a través de la fachada Game.
- project-domain-class-diagram.puml representa la estructura actual del dominio, con las clases y relaciones existentes en el código.
- collision-subsystem.puml muestra la detección geométrica, la resolución física y los estados del participante.
- participant-state-contact-sequence.puml muestra el contacto de patada y la activación y recuperación del aturdimiento.
- capturas conserva las exportaciones y los PDF de referencia originales.

## Lectura del código actual

- Presentación: DOPOSoccerGUI y NavigationController organizan la interfaz Swing; la presentación principal aún no se comunica con el dominio.
- Banco de pruebas: MechanicsTestApp abre MechanicsTestPanel. MechanicsTestController adapta acciones usando solo Game; MechanicsTestPanel permite activar capas de dibujo para hitboxes, vector, animación de patada y HUD. Las posiciones y velocidades se obtienen de la partida y no se fuerzan desde la presentación. Vida y poder se indican como no disponibles mientras Game no exponga esos datos y el poder siga sin implementarse.
- Partida: Game crea y administra sesiones. GameSession mantiene cancha, participantes, marcador y modalidad; SoccerField contiene los balones y delega la física a CollisionFacade, que coordina CourtGeometry y los resolvers de colisión.
- Paquetes: las clases de dominio están en domain, salvo las hitboxes en domain.
- Geometría: `Ball`, `SessionParticipant` y `SoccerField` contienen hitboxes asociadas; solo las formas geométricas (`CircularHitbox` y `RectangularHitbox`) implementan `Hitbox`. Las relaciones se reflejan en el diagrama de clases.
- Modalidad: TypeOfMatch define la comprobación; ByGoals compara el límite de goles y ByTime controla el límite de tiempo y la muerte súbita.

Las reglas acordadas y las discrepancias entre ellas y la implementación están en docs/logica-proyecto.md.
