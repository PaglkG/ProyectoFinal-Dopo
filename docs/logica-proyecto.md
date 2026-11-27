# Lógica y decisiones del proyecto

> Estado: borrador en construcción. Este documento es la fuente de verdad para las reglas de negocio acordadas. No se debe deducir una regla a partir de un método vacío, una implementación provisional o una prueba antigua.

## Cómo mantener este documento

- Consultarlo antes de cambiar la lógica o crear pruebas que dependan de reglas de negocio.
- Preguntar a Pablo cuando falte una regla o admita más de una interpretación.
- Al confirmar o cambiar una regla, actualizar este documento en el mismo trabajo que el código y sus pruebas.
- Mantener separados los contratos confirmados, el estado de implementación y las decisiones pendientes.

## Reglas confirmadas por Pablo

### Arquitectura

- La capa de presentación solo puede comunicarse con la clase `Game`; no debe comunicarse directamente con otras clases del dominio.
- `GameSession` y todas sus clases hijas comparten una sola instancia. La primera creación determina la clase concreta y configura modalidad, reglas, participantes y poderes; las solicitudes posteriores reutilizan esa sesión e ignoran nuevos parámetros. `Game` mantiene una referencia a esa sesión compartida una vez iniciada.
- Las operaciones de `Game` sobre la partida única no reciben un objeto `GameSession` ni un índice de sesión: la fachada usa internamente la instancia administrada.
- `Game.resetSession` reinicia la ronda de la instancia administrada: centra y detiene los balones y recoloca los participantes. Conserva marcador, modalidad, tiempo transcurrido y estado de finalización.

### Persistencia

- `Game.save(File)` guarda la sesión administrada en el archivo indicado. El selector del banco propone inicialmente `partida.dat`.

### Identidad y lado de juego

- `Game.movePlayer(player, direction)` mueve al participante de la sesión única administrada. `true` significa derecha y `false` izquierda. Cada llamada aplica un paso de 1/60 segundo a 150 unidades/segundo multiplicadas por la estadística `speed` del participante.
- `Game.jumpPlayer(player)` solicita un salto por identidad. Se inicia desde el suelo o desde la cabeza de otro participante si el jugador puede actuar y tiene apoyo. El impulso vertical inicial tiene magnitud 121 unidades/s; la gravedad del salto es de 180 unidades/s². El salto desplaza la hitbox real y respeta los límites y contactos sólidos.
- Un participante que se desplaza durante el salto puede pasar por encima de un rival cuando sus pies alcanzan la altura de la coronilla del rival.
- Si un participante aterriza sobre otro, el de abajo puede seguir caminando. El de arriba conserva su posición horizontal hasta que se desplace por sus controles; si pierde el apoyo, cae. Desde la cabeza puede iniciar otro salto con el mismo impulso que desde el suelo.
- En `PlayerVsPlayer`, el jugador 1 y el jugador 2 ocupan posiciones fijas; esas posiciones no se intercambian entre partidas.
- En `PlayerVsBot`, las posiciones también son fijas. El parámetro `player` selecciona cuál jugador humano participa; no selecciona el lado.
- En `PlayerVsBot`, el humano ocupa siempre la izquierda y el bot la derecha, incluso si se selecciona al jugador 2.
- `Game.startSession` recibe una lista adicional de cadenas para los poderes. En `PlayerVsPlayer`, contiene los nombres completos de clase del poder derecho y luego el poder izquierdo. En `PlayerVsBot`, contiene el poder del humano a la izquierda, el poder del bot a la derecha y el perfil del bot (`Clumsy`, `Offensive`, `Defensive` o `Expert`), en ese orden.

### Personajes y poderes

- La fachada `Game` obtiene los catálogos de tipos de partido y poderes de registros de clases en los recursos del dominio. Personalización usa esos catálogos y cada personaje conserva su poder fijo.
- Se ofrecen al menos cuatro personajes con poderes únicos: Corea del Sur usa `domain.BlueAura`, Camerún usa `domain.Lightning`, Nigeria usa `domain.Tornado` y Estados Unidos usa `domain.Illusion`.
- `BlueAura` viaja recto y horizontal a alta velocidad; al impactar empuja al rival hacia su propio arco. Su efecto dura 1,5 segundos y causa 8 puntos de daño base.
- `Lightning` viaja recto cubierto de electricidad; al impactar impide saltar y reduce a la mitad la velocidad del rival durante 3 segundos. Causa 12 puntos de daño base.
- `Tornado` viaja a ras del suelo y rebota rápidamente; al impactar lanza al rival al aire y le impide moverse, saltar o patear durante 3 segundos. Causa 8 puntos de daño base.
- `Illusion` divide visualmente el balón en ocho copias idénticas; siete son falsas y desaparecen al terminar el efecto de 2,5 segundos. Confunde al rival, que no puede distinguir el balón real. Causa 6 puntos de daño base.
- La barra de poder se llena en 10 segundos. Recibir un gol agrega inmediatamente un cuarto de carga. Con la barra llena, activar el poder vacía la barra e inicia su recarga; el participante conserva un aura visible sin límite temporal hasta que toca el balón y ejecuta el tiro, momento en que el aura desaparece.
- Si el tiro de poder golpea al rival, causa `dañoBase × multiplicadorKick` y aplica su efecto. Si entra al arco sin tocar al rival, cuenta como gol y no causa daño ni efecto. Mientras conserva un poder activo, el participante no puede activar otro. Los efectos no se acumulan: uno nuevo reemplaza al anterior.
- La vida no baja de cero. Al llegar a cero por una patada o tiro de poder, el jugador queda aturdido durante 3 segundos; no puede moverse, saltar ni patear, y al terminar recupera su vida máxima.
- La vida base es 100. `survival` determina la vida máxima con `vidaMáxima = 100 × (1 + 0,10 × (survival - 1))`; cada nivel aumenta en 10 tanto la vida máxima como la vida actual. La vida no baja de cero.
- Una patada normal causa `10 × multiplicadorKick` de daño y un tiro de poder causa `dañoBase × multiplicadorKick`. `multiplicadorKick = 1 + 0,10 × (kick - 1)`; los niveles de `kick` del 1 al 10 corresponden a multiplicadores de 1,0 a 1,9 y daño normal de 10 a 19.
- Una patada alcanza a un rival si la distancia entre las hitboxes es de 8 unidades o menos. Si rival y balón están simultáneamente en alcance, la patada golpea primero al rival. Al llegar a cero se aplica `StatusEffect.STUNNED` durante 3 segundos; el participante no puede moverse ni patear mientras esté activo y recupera 100 de vida cuando expira.
- Los perfiles de bot se diferencian por su prioridad: `Clumsy` elige movimientos al azar, `Offensive` prioriza anotar, `Defensive` prioriza defender el arco y `Expert` decide según el estado del partido.
- Los cuatro perfiles pueden saltar cuando el rival les bloquea un paso. También saltan si hay un balón alto al que podrían llegar durante el salto y ninguno está ya al alcance de la patada. Usan las mismas condiciones de apoyo que los jugadores humanos.

### Tiempo y modalidad por tiempo

- El tiempo del juego representa tiempo transcurrido y comienza en cero.
- En `ByTime.check`, `true` significa que la partida continúa.
- Si se alcanza el límite y el marcador está empatado, la partida continúa en muerte súbita.
- Si se alcanza el límite y el marcador está desigual, `ByTime.check` devuelve `false` y el partido termina.
- Cuando se alcanza el límite con el marcador empatado, `ByTime.suddenDeath` se activa y `check` devuelve `true`.
- Cuando un marcador desigual termina la muerte súbita, `ByTime.suddenDeath` vuelve a `false`.

### Balón y cancha

- El balón que crea `SoccerField` tiene radio 10 unidades.

### Modalidad por goles

- `ByGoals` termina el partido cuando uno de los marcadores iguala exactamente el límite configurado. Superarlo no cuenta como igualdad con el límite.
- La comparación de goles permanece en `ByGoals`; las comprobaciones de tiempo y muerte súbita permanecen en `ByTime`, aunque el diagrama ubica esos helpers bajo `ByGoals`.

### Reinicio del balón

- `Ball.resetBall(width, height)` sitúa el balón en el centro geométrico de la cancha (`x = width / 2`, `y = height / 2`) y deja `velocityX` y `velocityY` en cero.
- `SoccerField.resetBall(x, y)` ubica el centro de cada balón en esas coordenadas y deja sus velocidades en cero.

### Actualización, contactos y goles

- `GameSession.updateGame()` avanza en pasos fijos de `1/60` segundo. En cada paso, el balón suma `40 × paso` a su velocidad vertical y luego se desplaza usando las velocidades resultantes.
- El contacto normal del balón con un participante solo produce la respuesta física. Una patada que deja al rival con cero de vida activa el aturdimiento. La implementación del tiro de poder sigue pendiente.
- Al tocar el suelo, el balón se coloca con su parte inferior sobre el suelo, invierte la velocidad vertical y conserva el 70 % de su magnitud. Si la nueva magnitud es menor que `0.5`, la velocidad vertical queda en cero. La velocidad horizontal se conserva.
- Los balones son sólidos entre sí. Al tocarse, la cancha corrige la penetración y aplica una colisión de masas iguales con coeficiente de restitución `0.70` sobre la normal del contacto; la componente tangencial se conserva. Si ya se separan, solo se corrige la penetración y no se aplica un segundo impulso.
- Un participante no puede atravesar el balón al desplazarse: al caminar empuja en la misma dirección cualquier balón en contacto, esté quieto o en movimiento, en pasos pequeños y sin superposición. El empuje no cambia la velocidad del balón. Se detiene si sacaría el balón de la cancha o lo superpondría con otro balón o participante. Un participante que ya está en contacto puede alejarse sin mover el balón.
- Para una resolución entre círculo y rectángulo, la detección conserva la geometría circular exacta del balón. Las proyecciones AABB del círculo y del rectángulo, calculadas en la posición actual y la anterior, clasifican el eje del contacto: si solo X estaba separado antes, se corrige X; si solo Y estaba separado, se corrige Y. Si el estado previo es ambiguo, se corrige el eje con menor solapamiento actual; un empate se decide por la componente de velocidad de mayor magnitud. La posición se corrige hasta que el círculo real toque el rectángulo sobre ese eje; el balón no se convierte en caja.
- Durante cada actualización, un balón que toca a un participante rebota contra su hitbox rectangular. El participante se trata como una superficie fija; solo se refleja la componente de velocidad del eje resuelto y se conserva la tangencial, con restitución `0.70`, si el balón se aproxima. Si un jugador desciende sobre el balón, permanece encima y el balón se mueve hacia abajo a una velocidad mínima de `0.5` unidades/s.
- Cuando un balón queda apoyado sobre la cabeza, rueda a 4 unidades/s hacia el borde más cercano, o hacia la derecha si está centrado. Durante el salto acompaña al jugador mientras conserve el apoyo. Al salir del borde, la gravedad continúa su caída.
- Las colisiones entre balones conservan detección círculo-círculo exacta, corrección sobre la normal y respuesta de masas iguales con restitución `0.70`; no usan la clasificación AABB.
- Los contactos rectángulo-rectángulo se detectan mediante solapamiento AABB. El contacto entre participantes, sean aliados o rivales, bloquea el paso que intentaría penetrar, sin lanzar una excepción ni detener la simulación. Si ya estaban superpuestos, pueden dar pasos de separación.
- Un gol ocurre cuando el centro del balón entra en el rectángulo de gol dentro de la cancha. Las hitboxes de gol conservan ancho `cancha/10` y alto `cancha/3`, y quedan apoyadas sobre la línea del suelo dentro del límite inferior. La entrada frontal del arco permanece libre para permitir el gol; los bordes del arco rebotan el balón. El arco izquierdo concede un punto al lado derecho y viceversa. La detección del gol precede a la comprobación de salida del balón.
- Después de un gol se reinicia el balón al centro y se ubican los participantes de forma simétrica, con centros horizontales en `1/4` y `3/4` del ancho y las hitboxes apoyadas sobre el suelo.
- Los participantes no pueden entrar ni atravesar el rectángulo de un arco. Se bloquea el paso al arco sin detener la simulación. El balón puede entrar por la boca para marcar.
- Una colisión que empuje el balón contra un límite lo mantiene dentro del área jugable y lo rebota hacia el interior. Un participante situado fuera del rectángulo exterior o tocando el límite superior o lateral produce `GameException` con mensaje `objeto fuera de la partida`.
- El balón rebota contra laterales, techo, arcos y suelo con retención de `0.70`. Después de una colisión, la cancha corrige cualquier salida de los límites interiores y devuelve el balón hacia dentro. Al caminar, el participante puede empujar en su dirección cualquier balón en contacto, quieto o en movimiento, conservando la velocidad del balón; durante la actualización, el contacto físico con participantes se resuelve como rebote.
- La sesión conserva goles izquierdo y derecho. En `ByGoals`, alcanzar el límite termina la sesión; en `ByTime`, el tiempo transcurrido se cuenta en segundos y se pasan a `check` los minutos enteros completos. La modalidad temporal continúa en empate (muerte súbita) y termina al quedar un ganador una vez agotado el límite. Una sesión terminada no avanza más.

### Hitboxes y medidas provisionales

- Las entidades que participan en colisiones contienen una hitbox geométrica asociada y no implementan `Hitbox`. `Ball` contiene `CircularHitbox`, `SessionParticipant` contiene `RectangularHitbox` y `SoccerField` contiene las formas de la cancha y los arcos. Las formas son objetos geométricos, no entidades de juego independientes.
- Dos hitboxes cuentan como intersecadas cuando sus bordes se tocan. Dos círculos intersectan si la distancia entre centros es menor o igual a la suma de sus radios; dos rectángulos intersectan si sus intervalos se solapan incluyendo el contacto; círculo-rectángulo se resuelve por la distancia entre el centro del círculo y el punto más cercano del rectángulo.
- Solo las formas geométricas implementan `Hitbox`. `Hitbox.intersects(target)` y `HitboxIntersections.intersects(first, second)` centralizan la detección y devuelven un `HitboxContact` con el nombre simple de la entidad contactada y la hitbox asociada que participó. Sin contacto devuelven `name = ""` y `hitbox = null`. Los nombres usados son `Ball`, `SessionParticipant` y `SoccerField`.
- `Ball` y `SessionParticipant` consultan la geometría centralizada usando sus hitboxes asociadas. `SoccerField.intersects(target)` busca contacto con sus límites, arcos y balones; si la cancha es el objeto contactado, devuelve la parte concreta de la cancha o el balón contenido que intervino.
- `Hitbox.reposition(x, y)` reposiciona la referencia de cada forma: centro para círculos y esquina superior izquierda para rectángulos. Los métodos de movimiento de `Ball` y `SessionParticipant` actualizan sus hitboxes asociadas; `SoccerField` reposiciona las formas que contiene.
- `SoccerField` mantiene una sola `RectangularHitbox` como rectángulo exterior. Sus límites superior, inferior y laterales se derivan de los cuatro bordes y del grosor configurado; los límites derivados no son hitboxes almacenadas independientes.
- Como propuesta provisional, la cancha mide 450 × 150 unidades, los límites tienen grosor `alto / 100`, y cada arco mide `ancho / 10` por `alto / 3` y queda apoyado sobre el límite inferior dentro de la cancha.
- La hitbox de un participante en la cancha cubre desde la coronilla hasta los pies. Conserva el ancho `ancho de cancha / 20` y mide el `22,12 %` de la altura de la cancha, según las proporciones de los sprites.

### Configuración del jugador

- El dominio no almacena preferencias de sonido, música ni asignaciones de teclas. La presentación conserva una pantalla de configuración local; sus controles no se persisten y el botón Guardar solo vuelve al menú.

### Estadísticas actuales

- `Stats.action` es un `Map<String, Integer>` con `speed`, `jump`, `kick` y `survival` inicializados en 1; cada estadística puede subir hasta nivel 10. `points` inicia en 0.
- Al terminar un partido, cada jugador humano recibe 100 puntos por victoria y 25 por derrota. En empate, cada jugador humano recibe 25 puntos. En jugador contra bot, el bot no recibe puntos.
- Los puntos y niveles se conservan para el mismo ID de jugador entre sesiones administradas por una misma instancia de `Game`.
- Cada mejora válida aumenta en 1 el nivel de `speed`, `jump`, `kick` o `survival`. La primera compra cuesta 500 puntos y cada compra posterior cuesta el doble que la anterior, sin importar qué estadística se mejore. Si el saldo no alcanza o la habilidad no existe, se lanza `GameException` y no se modifica el estado. Una estadística en nivel 10 no cambia con otra solicitud de mejora.
- Si `survival` se mejora durante una partida, aumentan en 10 tanto la vida máxima como la vida actual. La tienda no debería permitir comprar mejoras durante un partido.
- En `GameSession.improveStatistics`, `true` selecciona el lado derecho/jugador 1 y `false` el lado izquierdo/jugador 2.

### Excepciones

- Toda excepción que lance explícitamente el código de la aplicación debe ser `domain.GameException`, con un mensaje definido en esa clase. Los errores de formato, índices e inputs de reglas también se convierten a `GameException`.

## Estado actual del código (no equivale a una regla confirmada)

- Las modalidades, los poderes, StatusEffect y GameException están directamente en domain desde el aplanado de paquetes.
- La presentación principal consulta y modifica el juego a través de la fachada `Game`, que expone getters escalares para dibujar. La subcapa `presentation.testing` también usa `Game` para consultar el estado, reiniciar, mover y patear participantes; no puede establecer posiciones ni velocidades directamente.
- La presentación consulta `Game` mediante getters escalares para posición, dimensiones, marcador y tiempo; la cancha se dibuja como un solo rectángulo. `movePlayer` y `kickPlayer` seleccionan identidad con booleano (`true`: jugador 1; `false`: jugador 2). El banco repite el movimiento horizontal mientras se mantiene la tecla y anima visualmente la pata al patear. `Game` expone puntos, vida actual, vida máxima y segundos restantes de recarga mediante getters por identidad. Los paneles todavía deben conectar esas consultas.
- `Game.getSessionTypes()`, `getMatchTypes()` y `getBotTypes()` devuelven nombres simples de las clases concretas disponibles; `getSessionType()` devuelve el nombre simple de la sesión activa.
- `DOPOSoccerGUI.startMatch` también lanza `IllegalStateException` si se intenta iniciar otra configuración tras fijar la sesión singleton. Esto es una excepción explícita de presentación que no coincide con la regla confirmada de usar `GameException` para las excepciones explícitas de la aplicación.
- `Game.movePlayer` delega en `GameSession`. La sesión comprueba pertenencia, estado de partida y aturdimiento, y comprueba todo el recorrido del paso antes de mover. Un paso que tocaría un lateral, otro participante o un arco queda bloqueado. El banco puede procesar ambos jugadores en el mismo ciclo y continuar si un paso está bloqueado. `SessionParticipant` aplica `150 × speed / 60`; `speed` inicia en 1.
- `Game.startSession` obtiene reflectivamente la subclase singleton de `GameSession`, le pasa `player`, la modalidad, sus parámetros y la lista ordenada de nombres de clase de poderes/perfil, y guarda la única referencia administrada.
- `PlayerVsBot` construye un jugador humano y un bot; `PlayerVsPlayer` construye dos jugadores humanos.
- PlayerVsPlayer y PlayerVsBot asocian los poderes elegidos mediante nombres completos de clase (domain.BlueAura, etc.); PlayerVsBot también crea el perfil indicado. `Power` comparte la recarga: inicia vacía, se completa en 600 pasos de 1/60 s, recibe 150 pasos por gol recibido y se limita al máximo. `GameSession.updateGame` actualiza ambos lados y no avanza una sesión terminada. `Power.usePower` consume únicamente una carga completa. `SessionParticipant.power` y `Game.powerPlayer` siguen sin conectar la activación; el aura, el bloqueo por poder activo, el tiro, el daño y los efectos específicos permanecen pendientes.
- `BotPlayer.decideAction` usa Template Method: comprueba si puede actuar, construye una observación y prioriza el salto hacia un balón alto alcanzable antes de solicitar otras acciones a su `BotDecisionStrategy`. `PlayerVsBot` ejecuta las acciones en el mismo paso de la sesión. Si un movimiento del bot queda bloqueado por un rival, la sesión solicita el salto con las mismas condiciones que aplica al humano. `Clumsy` mantiene su elección aleatoria durante 18 pasos, `Offensive` se acerca al balón y patea al alcance, `Defensive` protege el espacio entre balón y arco propio y despeja al alcance, y `Expert` reutiliza la defensa con ventaja y el ataque en empate o desventaja.
- `PlayerVsBot` trata una solicitud mantenida de patada como una sola pulsación, igual que el banco con la tecla humana. La siguiente solicitud se habilita cuando el bot deja de pedir la acción o se reinicia la ronda.
- El banco visual permite elegir partida manual o perfil de bot antes de crear la sesión única. En modo bot, el participante izquierdo se controla desde el banco y el derecho recibe sus acciones del dominio.
- El banco solicita el salto a `Game.jumpPlayer` con `W` (izquierda) o flecha arriba (derecha, solo en modo manual). El personaje y su contorno se dibujan con la misma posición que devuelve `Game`. La sesión reinicia el salto al recolocar a los participantes.
- La sesión permite deslizarse en paralelo por un borde sin bloquear el paso. Al abrir un guardado con la geometría anterior, ajusta la altura de la hitbox conservando la posición de los pies. El banco libera teclas sostenidas al perder el foco o al pausar.
- Stats inicia `speed`, `jump`, `kick` y `survival` en 1. `speed` se usa para el movimiento horizontal; `kick` calcula el daño de patadas y tiros de poder; `survival` determina la vida máxima. `jump` todavía no tiene efecto conectado al flujo de juego actual.
- SessionParticipant.applyStatusEffect reemplaza efectos anteriores del mismo tipo, pero conserva simultáneamente efectos de tipos distintos. Esto no cumple la regla confirmada de que un efecto nuevo reemplaza al anterior sin acumulación.
- `PlayerVsPlayer` coloca al jugador 1 a la derecha y al jugador 2 a la izquierda. `PlayerVsBot` coloca al humano a la izquierda y al bot a la derecha para ambas identidades humanas. Al abrir una partida antigua con los lados invertidos, refleja posiciones y velocidad horizontal del balón e intercambia los goles para conservar el resultado por participante.
- `GameSession` instancia una subclase de `TypeOfMatch` mediante reflexión.
- `Game` registra la referencia a la sesión singleton al iniciarla. Los niveles y puntos siguen asociados a los `SessionParticipant` de esa instancia; `Game` no mantiene un mapa duplicado de estadísticas.
- `Game.startSession` recibe en `parametersRules` la altura como primer parámetro, la anchura como segundo y el parámetro propio de la modalidad como tercero. `ByGoals` usa el tercero como límite de goles; `ByTime` lo usa como minutos del partido. `ByTime.check` recibe los marcadores en las posiciones 0 y 1 y el tiempo transcurrido en la posición 2.
- `ByTime.suddenDeath` se activa cuando se agota el tiempo con empate; en ese caso `check` devuelve `true` y, si un marcador queda desigual, se apaga al terminar.
- En el código actual no existen Scoreboard, Outfit, Head, Shoe, Rule ni Side en el dominio. `BotPlayer.decideAction` conserva su tipo de retorno `Set<String>` para nombres de acciones.
- Se conservan los parámetros de jugador y reglas en los constructores de `GameSession`, `PlayerVsBot` y `PlayerVsPlayer`, y la lista de parámetros en las fábricas reflectivas. `TypeOfMatch.check` conserva su contrato `boolean` con parámetros, de acuerdo con la regla de continuidad confirmada.
- `GameSession` coordina el paso de juego, el movimiento, las validaciones de participantes y los goles. `SoccerField` contiene y avanza los balones y delega las colisiones en `CollisionFacade`; esta coordina `CourtGeometry`, `BallCollisionResolver` y `BallParticipantCollisionResolver`. `Ball` conserva el estado físico y ejecuta las operaciones delegadas. `GameSession` no depende directamente de la fachada.
- En cada paso, la sesión actualiza los efectos, avanza los saltos y avanza los balones. Comprueba que los participantes estén dentro del área permitida, actualiza los balones apoyados, recupera los contactos que atravesó un balón y comprueba si hay gol. Si hubo gol, reinicia la ronda y termina la fase de colisiones de ese paso. En otro caso, resuelve primero los rebotes entre balones, después el contacto entre balones y participantes y, finalmente, los rebotes contra los laterales, el techo, los marcos y el suelo. La última corrección mantiene cada balón dentro del área jugable. El empuje al caminar se procesa al mover al participante.
- Un solo detector geométrico calcula el contacto de círculos y rectángulos. El contorno de la cancha y los marcos de los arcos se resuelven como geometría fija durante el paso.
- El participante delega el movimiento y el permiso para patear según su estado normal o aturdido. Cuando el daño deja su vida en cero, el estado aturdido inicia o renueva el efecto. Al vencer, el participante recupera su vida máxima. El estado se obtiene de sus efectos activos, que ya forman parte de la partida guardada.
- En el código actual, `Ball`, `SessionParticipant` y `SoccerField` no implementan `Hitbox`; cada uno conserva su forma o formas asociadas. Solo `CircularHitbox` y `RectangularHitbox` implementan esa interfaz. Las entidades delegan la detección y la matemática geométrica permanece en los colaboradores de colisión.
- `Ball` conserva su centro anterior antes de cada avance, y `SessionParticipant` conserva su posición anterior antes de moverse o reposicionarse. `resetPosition` y la ubicación simétrica sincronizan la posición previa con la nueva coordenada para que un reinicio no se interprete como movimiento. `CircleRectangleCollisionResolver` detecta el contacto círculo-rectángulo con el círculo real, usa las proyecciones AABB actuales/previas para clasificar el eje y separa el círculo hasta el contacto; los resolutores aplican después la respuesta física correspondiente.
- Antes de comprobar el gol, el resolutor de balón contra participante recupera los contactos que el balón cruzó entre dos posiciones. Usa el recorrido relativo y el radio real, incluida la forma redondeada de las esquinas. Corrige la posición al primer contacto entre los participantes interceptados y la fase de colisiones aplica la restitución vigente.
- `GameSession.kickPlayer` permite la patada a participantes registrados en sesiones activas, si no están aturdidos. Prioriza dañar al rival si está dentro de 8 unidades entre hitboxes; sin rival al alcance, permite patear el balón cuando su superficie está a 8 unidades o menos del borde de la hitbox. El dominio aplica al balón un impulso horizontal de 150 unidades/s hacia el arco contrario y uno vertical de -50 unidades/s.

## Decisiones pendientes

### Inicio y participantes


- ¿Una sesión inicia inmediatamente con los participantes predeterminados o debe esperar a que se registren los usuarios?
- ¿Qué debe ocurrir si se intenta crear una sesión con listas nulas, vacías o parámetros incompletos?



### Marcador, estadísticas y poderes

- Definir el efecto de la mejora `jump`; hoy solo se incrementa su nivel. `speed` multiplica el movimiento, `kick` aumenta el daño y `survival` la vida máxima.

### Bots, reglas y persistencia

- ¿Qué reglas genéricas se pueden agregar/quitar y en qué orden se aplican?
- `open(File)` deserializa una partida guardada y reemplaza la sesión única administrada.
- El guardado conserva la sesión completa: modalidad, marcador, tiempo, cancha, participantes, balones, estadísticas y objetos asociados. Los errores de archivo se reportan mediante `GameException` con los códigos `INVALID_SAVE_FILE`, `SAVE_FAILED` o `OPEN_FAILED`.

## Registro de decisiones

| Fecha | Decisión | Confirmada por |
|---|---|---|
| 2026-11-10 | Las posiciones de `PlayerVsPlayer` son fijas; en `PlayerVsBot`, el humano siempre ocupa la derecha y el bot la izquierda, mientras `player` selecciona la identidad humana. El tiempo transcurrido inicia en cero; `ByTime.check` devuelve `true` para continuar y mantiene empate en muerte súbita. | Pablo |
| 2026-11-11 | `Ball.resetBall(width, height)` centra el balón geométricamente y pone ambas componentes de velocidad en cero. | Pablo |
| 2026-11-11 | Se retira el subsistema de configuración del jugador, incluidas preferencias de sonido, música y asignaciones de teclas. | Pablo |
| 2026-11-12 | `ByGoals` termina al igualar el límite; `ByTime.suddenDeath` se activa en empate al agotarse el tiempo; el campo reinicia cada balón a un centro `(x,y)` y deja su velocidad en cero. | Pablo |
| 2026-11-12 | `Hitbox.reposition(x,y)` mueve círculos por su centro y rectángulos por su esquina superior izquierda; las intersecciones y sus efectos quedan pendientes. | Pablo |
| 2026-11-12 | `Stats.action` usa un mapa con `speed`, `goal`, `kick` y `jump` inicializados en 1; `points` inicia en 0. | Pablo |
| 2026-11-15 | Las intersecciones geométricas incluyen el contacto entre bordes; la consulta devuelve la entidad contactada y su hitbox asociada, o nombre vacío y hitbox nula si no hay contacto. Los nombres son `Ball`, `SessionParticipant` y `SoccerField`; la cancha considera sus límites, arcos y balones. | Pablo |
| 2026-11-15 | Se confirma el paso fijo de 1/60 s, gravedad 3.14, rebote con retención 70 % y umbral 0.5; impulso participante-balón ±4 horizontal y -1 vertical una vez por contacto; gol al entrar el centro en el arco, punto para el lado opuesto y reinicio simétrico. Se confirman las dos excepciones literales para objetos fuera/superpuestos y la actualización temporal ByTime en segundos con minutos enteros completados. | Pablo |
| 2026-11-15 | Se reemplaza la gravedad anterior por 40 unidades/s². El balón se separa horizontalmente del participante hasta salir de su hitbox; al tocarlo se redirige hacia fuera con impulso horizontal de magnitud 4 y suma impulso vertical -1 una vez por contacto. | Pablo |
| 2026-11-16 | El balón creado por `SoccerField` tiene radio 10 unidades. | Pablo |
| 2026-11-17 | Se definen los poderes BlueAura, Lightning, Tornado e Illusion con sus trayectorias, efectos, duración y daño base del anexo del PDF; el orden y formato de la lista de poderes/perfil al iniciar sesión; y las prioridades de los cuatro perfiles de bot. | Pablo |
| 2026-11-17 | Se confirma que `Game.movePlayer` usa `true` para derecha y `false` para izquierda, y aplica por llamada un paso de 1/60 s a 150 unidades/s multiplicadas por `speed`. | Pablo |
| 2026-11-21 | En el banco de mecánicas, el balón apoyado sobre la cabeza acompaña al jugador durante el salto y vuelve a la física normal al aterrizar; aumentar 10 % la velocidad inicial visual del salto (de 110 a 121), sin cambiar la gravedad. | Pablo |
| 2026-11-19 | `GameSession` comparte una única instancia entre todas sus clases hijas; la primera clase concreta creada permanece y las solicitudes posteriores la reutilizan. | Pablo |
| 2026-11-19 | Reiniciar la sesión reinicia solo la ronda: balón al centro y participantes a sus posiciones; marcador, modalidad, tiempo y estado de finalización se conservan. | Pablo |
| 2026-11-17 | `GameSession.improveStatistics(true, ...)` selecciona derecha/jugador 1 y `false` izquierda/jugador 2. Las excepciones explícitas de la aplicación son `GameException`. | Pablo |
| 2026-11-17 | Los puntos y niveles se conservan para el mismo ID de jugador entre sesiones administradas por una misma instancia de `Game`. | Pablo |
| 2026-11-17 | Alcanzar cero activa un aturdimiento de 3 segundos con `StatusEffect` y recuperación de vida al expirar. | Pablo |
| 2026-11-17 | Se reemplaza la respuesta de contacto participante-balón: al caminar, el jugador puede empujar balones quietos en pasos pequeños sin atravesarlos; el empuje no cambia su velocidad. El empuje se bloquea si causaría colisión con la cancha u otra entidad. | Pablo |
| 2026-11-17 | Los balones rebotan contra participantes con restitución de `0.70`. Si el jugador cae sobre el balón, permanece encima y el balón adquiere movimiento hacia abajo, con mínimo de `0.5` unidades/s. | Pablo |
| 2026-11-20 | `parametersRules` de `Game.startSession` ordena altura, anchura y parámetro propio de modalidad; `ByGoals` usa el tercero como límite de goles y `ByTime` como minutos. | Pablo |
| 2026-11-20 | `SoccerField` conserva una única hitbox rectangular exterior; los cuatro límites se derivan de sus bordes. | Pablo |
| 2026-11-21 | La resolución círculo-rectángulo conserva la geometría circular exacta, clasifica el eje con proyecciones AABB actuales y previas y corrige el círculo real sobre el eje elegido; las colisiones círculo-círculo y las excepciones de participantes conservan sus reglas actuales. | Pablo |
| 2026-11-21 | Al caminar, el jugador puede desplazar en su dirección balones en contacto, incluso si están en movimiento, conservando su velocidad actual y respetando límites y otras entidades. | Pablo |
| 2026-11-21 | Las estadísticas son `speed`, `jump`, `kick` y `survival`, inicializadas en 1 y limitadas a nivel 10; `survival` aumenta la vida máxima y actual 10 por nivel, y `kick` aplica un multiplicador de 10 % por nivel a patadas y tiros de poder. Cada partido da 100 puntos por victoria y 25 por derrota; el empate da 25 a cada jugador. Las mejoras cuestan 500 puntos al inicio y duplican su costo en cada compra del jugador. | Pablo |
| 2026-11-24 | El banco propone el nombre `partida.dat`; `Game.save(File)` conserva el destino proporcionado. | Pablo |
| 2026-11-24 | Se unifica la detección geométrica y se retira el registro de estrategias. La cancha se trata como geometría fija. Se conservan los rebotes entre balones y en los marcos de los arcos, y el empuje al caminar. El estado normal o aturdido controla las acciones del participante. El contacto normal con el balón solo produce respuesta física. | Pablo |
| 2026-11-26 | El humano ocupa la izquierda y el bot la derecha. Los contactos entre participantes ya no producen una excepción de superposición. Los jugadores no pueden atravesar los arcos. | Pablo |
| 2026-11-26 | El salto mueve la hitbox física con impulso inicial de 121 unidades/s y gravedad de 180 unidades/s². El balón apoyado acompaña al jugador y rueda hacia el borde más cercano a 4 unidades/s, hacia la derecha si parte centrado. | Pablo |
| 2026-11-26 | La hitbox cubre desde la coronilla hasta los pies y conserva el ancho anterior. | Pablo |
| 2026-11-26 | El humano y el bot pueden pasar sobre un rival durante el salto. El bot solicita el salto al topar con el rival o detectar un balón alto alcanzable. Se conservan el impulso, la gravedad del salto y la altura del arco. | Pablo |
| 2026-11-26 | Cuando un jugador está sobre otro, el de abajo puede caminar; el de arriba puede saltar y cae si pierde el apoyo. El movimiento de abajo no arrastra al de arriba. | Pablo |
