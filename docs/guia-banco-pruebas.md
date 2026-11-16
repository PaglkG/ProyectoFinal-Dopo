# Banco visual de pruebas de mecánicas

La ventana de prueba es independiente de la aplicación principal. Para abrirla desde el IDE, ejecuta `presentation.testing.MechanicsTestApp`.

En la parte superior se puede iniciar una sesión de dos jugadores o de jugador contra bot, elegir modalidad por goles o por tiempo y definir su límite. La sesión nueva comienza a simular automáticamente; **Pausar simulación** detiene el reloj y **Un paso** avanza una sola actualización del dominio. Los escenarios de la derecha colocan los objetos en una posición preparada y ejecutan la actualización real de `GameSession`; los casos sin excepción continúan animándose para que se vea el resultado.

Los escenarios cubren goles en ambos arcos, contacto jugador-balón, rebote del suelo, contacto seguro con un límite, balón fuera, participante fuera, participante tocando un límite lateral, participantes superpuestos y victoria durante muerte súbita. El marcador, el tiempo, las posiciones y el mensaje de excepción se muestran en la ventana.

El movimiento y el salto son acciones de esta pantalla; no implementan `SessionParticipant.move()`. Los botones **Patear jugador izquierdo (F)** y **Patear jugador derecho (Enter)** envían la acción al dominio. La patada se aplica si la superficie del balón está a 8 unidades o menos del borde de la hitbox del jugador: el dominio suma un impulso horizontal de 150 unidades/s hacia el arco contrario y uno vertical de -50 unidades/s. En jugador contra bot, solo el humano de la derecha puede patear. La pantalla muestra el mensaje **La partida terminó** cuando el dominio indica que la sesión finalizó. La simulación de movimiento de esta pantalla usa 150 unidades/s de velocidad lateral, impulso vertical inicial de -110 unidades/s y gravedad de 180 unidades/s²; mantiene a los participantes dentro de la cancha y evita que se atraviesen. Los poderes siguen pendientes.

El marcador visible muestra los goles izquierdo y derecho, que registra `GameSession` cuando el centro del balón entra en un arco. El arco izquierdo suma para la derecha y el derecho suma para la izquierda; al anotar se reinicia la ronda según las reglas del dominio.
