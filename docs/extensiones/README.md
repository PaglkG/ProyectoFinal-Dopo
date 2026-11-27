# Cómo extender DOPOSoccer

Estas guías parten del código actual. Cada una indica dónde registrar la extensión, qué métodos siguen el dato y qué probar. Los ejemplos describen cambios futuros. Las reglas confirmadas están en [Lógica del proyecto](../logica-proyecto.md).

| Quieres agregar | Guía |
| --- | --- |
| Un personaje con país, imágenes y poder fijo, o una clase nueva de jugador | [Personajes y jugadores](personajes.md) |
| Una conducta automática nueva | [Perfiles de bot](bots.md) |
| Otra composición de participantes | [Tipos de juego](tipos-de-juego.md) |
| Otra condición de finalización | [Tipos de partida](tipos-de-partida.md) |
| Un poder nuevo, incluido un tiro fuerte que solo afecta al balón | [Poderes y colisiones](poderes.md) |
| Conectar las teclas y el movimiento de los personajes en la cancha oficial | [Movimiento en presentación](movimiento-presentacion.md) |
| Leer, mostrar y comprar mejoras desde la tienda | [Tienda de mejoras](tienda.md) |

Flujo de presentación oficial. Personalización construye `MatchConfiguration`. La ventana inicia la sesión mediante `Game`. En cada paso publica `MatchSnapshot`, y los paneles consultan esa captura. El banco tiene su propio controlador y lee de su fachada `Game`. Consulta la [guía general de presentación](../guia-presentacion.md) para ubicar cada punto de entrada.
