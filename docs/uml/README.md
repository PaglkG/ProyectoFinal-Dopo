# UML del proyecto

Las fuentes PlantUML y las capturas se conservan para contrastar el diseño recibido con el código. La reorganización del dominio aplanó todos sus paquetes salvo domain.

- domain-class-diagram.puml y domain-sequences.puml representan el modelo de referencia recibido, no el inventario vigente del código. Sus diferencias conocidas se enumeran en domain-review.md.
- package-diagram.puml y package-diagram.png son anteriores al aplanado del dominio y no representan los paquetes actuales.
- Las secuencias start-session, player-vs-bot, player-vs-player y by-time-check describen flujos implementados, pero se deben contrastar con el código y la lógica documentada antes de tratarlas como contratos.
- mechanics-test-sequence.puml representa el banco visual a través de Game y el salto físico de la hitbox.
- presentation-save-open-sequence.puml representa el guardado desde la pausa y la apertura desde el menú principal.
- presentation-state-sequence.puml muestra cómo la ventana publica el estado de cada paso y carga partidas guardadas para dejarlas en pausa.
- bot-turn-sequence.puml muestra cómo la sesión consulta la estrategia del bot y ejecuta su decisión en cada paso.
- project-domain-class-diagram.puml representa la estructura actual del dominio, con las clases y relaciones existentes en el código.
- collision-subsystem.puml muestra la detección geométrica, la resolución física y los estados del participante.
- participant-state-contact-sequence.puml muestra el contacto de patada y la activación y recuperación del aturdimiento.
- capturas conserva las exportaciones y los PDF de referencia originales.

## Lectura del código actual

- Presentación: DOPOSoccerGUI consulta Game y publica capturas inmutables en PresentationState. NavigationController organiza los cambios de pantalla y los paneles consultan el estado compartido.
- Banco de pruebas: MechanicsTestApp consulta los perfiles a través de Game y permite elegir dos jugadores o un bot antes de iniciar la sesión. MechanicsTestController adapta las acciones manuales usando Game. En el modo contra bot, la persona usa el lado izquierdo y la sesión controla el derecho. MechanicsTestPanel permite activar capas para hitboxes, vector, animación de patada y HUD. Las posiciones y velocidades se obtienen de la partida.
- Partida: Game crea y administra sesiones. GameSession mantiene cancha, participantes, marcador y modalidad; SoccerField contiene los balones y delega la física a CollisionFacade, que coordina CourtGeometry y los resolvers de colisión.
- Paquetes: las clases de dominio están en domain, salvo las hitboxes en domain.
- Geometría: `Ball`, `SessionParticipant` y `SoccerField` contienen hitboxes asociadas; solo las formas geométricas (`CircularHitbox` y `RectangularHitbox`) implementan `Hitbox`. Las relaciones se reflejan en el diagrama de clases.
- Modalidad: TypeOfMatch define la comprobación; ByGoals compara el límite de goles y ByTime controla el límite de tiempo y la muerte súbita.
- `start-session-sequence.puml` muestra la creación de otra sesión una vez finaliza la activa y la transferencia de `Stats` por ID humano.

Las reglas acordadas y las discrepancias entre ellas y la implementación están en docs/logica-proyecto.md.
