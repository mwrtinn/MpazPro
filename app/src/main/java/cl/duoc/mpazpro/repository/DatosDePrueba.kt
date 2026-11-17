package cl.duoc.mpazpro.repository

import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.utils.Seguridad
import cl.duoc.mpazpro.model.TipoPregunta

/**
 * Datos ficticios y contenidos propios (sin reproducir material protegido), según el caso.
 * IMPORTANTE: los códigos y textos de los OA deben contrastarse con el Currículum Nacional
 * y ser autorizados por UTP antes de usarse en la entrega final.
 */
object DatosDePrueba {

    private const val REF = "Currículum Nacional - curriculumnacional.mineduc.cl (verificar código oficial)"

    fun usuarios() = listOf(
        // RUT ficticios con dígito verificador válido.
        UsuarioEntity("123456785", "Profesora Ana Soto", Seguridad.hash("1234"), Rol.DOCENTE),
        UsuarioEntity("134567899", "Jefa UTP Carla Rojas", Seguridad.hash("1234"), Rol.UTP),
        UsuarioEntity("111111111", "Estudiante Prueba 1", Seguridad.hash("1234"), Rol.ESTUDIANTE),
        UsuarioEntity("222222222", "Estudiante Prueba 2", Seguridad.hash("1234"), Rol.ESTUDIANTE)
    )

    /** Cada elemento: objetivo + sus contenidos + sus preguntas (el oaId real se asigna al insertar). */
    fun objetivos(): List<Triple<ObjetivoEntity, List<ContenidoEntity>, List<PreguntaEntity>>> = listOf(
        Triple(
            ObjetivoEntity(
                asignatura = "Matemática", unidad = "Unidad 1: Números hasta el 1.000", codigo = "MA03 OA 01",
                eje = "Números y operaciones",
                descripcion = "Contar números del 0 al 1.000 de 2 en 2, de 5 en 5, de 10 en 10 y de 100 en 100.",
                referencia = REF, asignado = true, intentosPrueba = 2
            ),
            listOf(
                c("Contar de 5 en 5 y de 10 en 10", "Contar de 5 en 5 es sumar 5 cada vez. Empezamos en 0 y vamos dando saltos: 5, 10, 15, 20. Contar de 10 en 10 es igual, pero con saltos de 10.", "Si cuento de 10 en 10: 10, 20, 30, 40, 50.", "🔢", EstadoContenido.APROBADO),
                c("Contar de 100 en 100", "Para contar de 100 en 100 sumamos 100 cada vez: 100, 200, 300, 400. ¡Así llegamos rápido al 1.000!", "100, 200, 300 y ahora sigue el 400.", "💯", EstadoContenido.PENDIENTE)
            ),
            listOf(
                q(TipoPregunta.DESAFIO, 1, "Cuenta de 2 en 2: 2, 4, 6, 8, ¿qué número sigue?", "9", "10", "12", 1),
                q(TipoPregunta.DESAFIO, 2, "Cuenta de 5 en 5: 5, 10, 15, ¿qué número sigue?", "20", "25", "30", 0),
                q(TipoPregunta.PRUEBA, 1, "Cuenta de 10 en 10: 10, 20, 30, 40, ¿qué número sigue?", "45", "50", "60", 1),
                q(TipoPregunta.PRUEBA, 2, "Cuenta de 100 en 100: 100, 200, 300, ¿qué número sigue?", "400", "350", "500", 0)
            )
        ),
        Triple(
            ObjetivoEntity(
                asignatura = "Lenguaje y Comunicación", unidad = "Unidad 1: Leemos cuentos", codigo = "LE03 OA 04",
                eje = "Lectura",
                descripcion = "Comprender textos narrativos breves: reconocer personajes, lugares y lo que ocurre.",
                referencia = REF, asignado = true, intentosPrueba = 2
            ),
            listOf(
                c("Leer un cuento corto", "Para entender un cuento busca quiénes son los personajes, dónde ocurre y qué pasa primero y después.", "«Ana ayudó a Toto». Los personajes son Ana y Toto.", "📖", EstadoContenido.APROBADO)
            ),
            listOf(
                q(TipoPregunta.DESAFIO, 1, "Lee: «Ana tiene un perro llamado Toto». ¿Cómo se llama el perro?", "Ana", "Toto", "Perro", 1),
                q(TipoPregunta.DESAFIO, 2, "Lee: «Llovía mucho, así que Ana se puso su paraguas». ¿Por qué se puso el paraguas?", "Llovía mucho", "Hacía calor", "Era de noche", 0),
                q(TipoPregunta.PRUEBA, 1, "Lee: «Toto corrió al jardín y jugó con la pelota». ¿Dónde jugó Toto?", "En la casa", "En el jardín", "En la escuela", 1),
                q(TipoPregunta.PRUEBA, 2, "Lee: «Ana estaba triste porque perdió su pelota». ¿Cómo se sentía Ana?", "Alegre", "Enojada", "Triste", 2)
            )
        ),
        Triple(
            ObjetivoEntity(
                asignatura = "Ciencias Naturales", unidad = "Unidad 1: Las plantas", codigo = "CN03 OA 01",
                eje = "Ciencias de la vida",
                descripcion = "Reconocer qué necesitan las plantas para vivir y qué función cumplen sus partes.",
                referencia = REF, asignado = false, intentosPrueba = 2
            ),
            listOf(
                c("Lo que necesitan las plantas", "Las plantas necesitan agua, luz y aire para crecer. Las raíces toman el agua del suelo y las hojas reciben la luz.", "Una planta junto a la ventana recibe luz y crece mejor.", "🌱", EstadoContenido.APROBADO)
            ),
            listOf(
                q(TipoPregunta.DESAFIO, 1, "¿Qué necesita una planta para crecer?", "Solo juguetes", "Agua y luz", "Hielo", 1),
                q(TipoPregunta.DESAFIO, 2, "Si una planta no recibe luz por muchos días, ¿qué le puede pasar?", "Crece más rápido", "Se pone más verde", "Se debilita", 2),
                q(TipoPregunta.PRUEBA, 1, "¿Qué parte de la planta toma el agua del suelo?", "Raíz", "Hoja", "Flor", 0),
                q(TipoPregunta.PRUEBA, 2, "¿Qué parte de la planta recibe la luz del sol?", "Raíz", "Hoja", "Semilla", 1)
            )
        ),
        Triple(
            ObjetivoEntity(
                asignatura = "Historia, Geografía y Ciencias Sociales", unidad = "Unidad 1: Nos ubicamos en el mapa", codigo = "HI03 OA 01",
                eje = "Geografía",
                descripcion = "Usar mapas y puntos cardinales para ubicar lugares.",
                referencia = REF, asignado = false, intentosPrueba = 2
            ),
            listOf(
                c("Los puntos cardinales", "Los puntos cardinales nos ayudan a orientarnos: Norte, Sur, Este y Oeste.", "El sol sale por el Este y se esconde por el Oeste.", "🧭", EstadoContenido.APROBADO)
            ),
            listOf(
                q(TipoPregunta.DESAFIO, 1, "¿Cuántos puntos cardinales hay?", "2", "3", "4", 2),
                q(TipoPregunta.DESAFIO, 2, "¿Por dónde sale el sol por la mañana?", "Este", "Oeste", "Norte", 0),
                q(TipoPregunta.PRUEBA, 1, "¿Qué nos muestra un mapa?", "Recetas", "Lugares y su ubicación", "Canciones", 1),
                q(TipoPregunta.PRUEBA, 2, "Los puntos cardinales son Norte, Sur, Este y...", "Centro", "Arriba", "Oeste", 2)
            )
        )
    )

    private fun c(titulo: String, explicacion: String, ejemplo: String, icono: String, estado: String) =
        ContenidoEntity(oaId = 0, titulo = titulo, explicacion = explicacion, ejemplo = ejemplo, icono = icono, estado = estado)

    private fun q(tipo: String, dif: Int, enunciado: String, a: String, b: String, cc: String, correcta: Int) =
        PreguntaEntity(oaId = 0, tipo = tipo, dificultad = dif, enunciado = enunciado, opcionA = a, opcionB = b, opcionC = cc, correcta = correcta)
}
