package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.remote.NaturalezaApi
import cl.duoc.mpazpro.data.remote.NaturalezaCliente
import cl.duoc.mpazpro.data.remote.FotoNaturalezaDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Información educativa de cada especie.
data class PlantaEducativa(
    val id: Int,
    val nombre: String,
    val cientifico: String,
    val datoCurioso: String
)

// Pregunta generada con una fotografía externa.
data class PreguntaNaturaleza(
    val planta: PlantaEducativa,
    val imagen: String,
    val autor: String,
    val licencia: String,
    val fotoId: Long?,
    val alternativas: List<String>
)

// Estado general del minijuego.
data class NaturalezaUiState(
    val cargando: Boolean = false,
    val preguntas: List<PreguntaNaturaleza> = emptyList(),
    val indice: Int = 0,
    val seleccion: String? = null,
    val puntos: Int = 0,
    val terminado: Boolean = false,
    val error: String? = null
) {

    val preguntaActual: PreguntaNaturaleza?
        get() = preguntas.getOrNull(indice)

    val respondida: Boolean
        get() = seleccion != null

    val estrellas: Int
        get() = puntos / 10
}

// Gestiona el juego educativo de flora chilena.
class NaturalezaViewModel(
    private val api: NaturalezaApi = NaturalezaCliente.api
) : ViewModel() {

    private val _estado =
        MutableStateFlow(NaturalezaUiState())

    val estado = _estado.asStateFlow()

    // Catálogo controlado de plantas chilenas.
    private val catalogo = listOf(

        PlantaEducativa(
            id = 431220,
            nombre = "Copihue",
            cientifico = "Lapageria rosea",
            datoCurioso =
                "El copihue es la flor nacional de Chile. Crece como una planta trepadora en los bosques."
        ),

        PlantaEducativa(
            id = 126404,
            nombre = "Araucaria",
            cientifico = "Araucaria araucana",
            datoCurioso =
                "La araucaria también se conoce como pehuén. Sus semillas se llaman piñones."
        ),

        PlantaEducativa(
            id = 123353,
            nombre = "Palma chilena",
            cientifico = "Jubaea chilensis",
            datoCurioso =
                "La palma chilena tiene un tronco grueso y crece naturalmente en la zona central de Chile."
        ),

        PlantaEducativa(
            id = 407502,
            nombre = "Ñirre",
            cientifico = "Nothofagus antarctica",
            datoCurioso =
                "El ñirre crece en el sur de Chile y pierde sus hojas durante el otoño."
        ),

        PlantaEducativa(
            id = 533508,
            nombre = "Guayacán",
            cientifico = "Porlieria chilensis",
            datoCurioso =
                "El guayacán puede vivir en ambientes secos de Chile. Debemos protegerlo y cuidar su hábitat."
        )
    )

    // Solo permite fotografías con licencias reutilizables.
    private val licenciasPermitidas = setOf(
        "cc0",
        "cc-by",
        "cc-by-sa",
        "cc-by-nc",
        "cc-by-nc-sa"
    )

    private fun obtenerImagen(
        foto: FotoNaturalezaDto
    ): String? {

        val url = foto.imagenMediana
            ?: foto.url?.replace(
                "/square.",
                "/medium."
            )

        return url?.takeIf {
            it.startsWith("https://")
        }
    }

    private fun fotoValida(
        foto: FotoNaturalezaDto
    ): Boolean {

        return foto.licencia in licenciasPermitidas &&
                !foto.attribution.isNullOrBlank() &&
                obtenerImagen(foto) != null
    }

    // Consulta las especies e inicia una partida.
    fun iniciarJuego() {

        if (_estado.value.cargando) return

        _estado.value = NaturalezaUiState(
            cargando = true
        )

        viewModelScope.launch {

            try {

                val ids = catalogo.joinToString(",") {
                    it.id.toString()
                }

                val respuesta = api.consultarEspecies(ids)

                val especies = respuesta.results.orEmpty()

                val preguntas = catalogo.mapNotNull { planta ->

                    val especie = especies.firstOrNull {
                        it.id == planta.id
                    } ?: return@mapNotNull null

                    val fotos = listOfNotNull(
                        especie.fotoPrincipal
                    ) + especie.fotos.orEmpty()
                        .mapNotNull { it.photo }

                    val foto = fotos.firstOrNull {
                        fotoValida(it)
                    } ?: return@mapNotNull null

                    val imagen = obtenerImagen(foto)
                        ?: return@mapNotNull null

                    // Genera tres alternativas incorrectas.
                    val incorrectas = catalogo
                        .filter { it.id != planta.id }
                        .map { it.nombre }
                        .shuffled()
                        .take(3)

                    val alternativas = (
                            incorrectas + planta.nombre
                            ).shuffled()

                    PreguntaNaturaleza(
                        planta = planta,
                        imagen = imagen,
                        autor = foto.attribution.orEmpty(),
                        licencia = foto.licencia.orEmpty(),
                        fotoId = foto.id,
                        alternativas = alternativas
                    )
                }.shuffled()

                // Evita iniciar partidas sin material suficiente.
                if (preguntas.size < 3) {

                    _estado.value = NaturalezaUiState(
                        error =
                            "No encontramos suficientes fotografías disponibles. Intenta nuevamente."
                    )

                } else {

                    _estado.value = NaturalezaUiState(
                        preguntas = preguntas
                    )
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: IOException) {

                _estado.value = NaturalezaUiState(
                    error =
                        "No se pudo conectar con iNaturalist. Verifica tu conexión a Internet."
                )

            } catch (e: HttpException) {

                _estado.value = NaturalezaUiState(
                    error =
                        "El servicio de naturaleza no está disponible (${e.code()})."
                )

            } catch (e: Exception) {

                _estado.value = NaturalezaUiState(
                    error =
                        "No se pudo iniciar el juego. Intenta nuevamente."
                )
            }
        }
    }

    // Registra una respuesta y entrega diez puntos por acierto.
    fun responder(alternativa: String) {

        _estado.update { actual ->

            val pregunta = actual.preguntaActual

            if (
                pregunta == null ||
                actual.respondida ||
                actual.terminado ||
                actual.cargando ||
                alternativa !in pregunta.alternativas
            ) {

                actual

            } else {

                val correcta =
                    alternativa == pregunta.planta.nombre

                actual.copy(
                    seleccion = alternativa,
                    puntos = actual.puntos +
                            if (correcta) 10 else 0
                )
            }
        }
    }

    // Avanza a la siguiente pregunta.
    fun siguiente() {

        _estado.update { actual ->

            if (
                !actual.respondida ||
                actual.terminado
            ) {

                actual

            } else if (
                actual.indice >=
                actual.preguntas.lastIndex
            ) {

                actual.copy(
                    terminado = true
                )

            } else {

                actual.copy(
                    indice = actual.indice + 1,
                    seleccion = null
                )
            }
        }
    }

    // Permite jugar otra vez sin repetir solicitudes a la API.
    fun volverAJugar() {

        val actual = _estado.value

        if (actual.cargando) return

        if (actual.preguntas.isEmpty()) {

            iniciarJuego()
            return
        }

        val preguntas = actual.preguntas
            .shuffled()
            .map {

                it.copy(
                    alternativas =
                        it.alternativas.shuffled()
                )
            }

        _estado.value = NaturalezaUiState(
            preguntas = preguntas
        )
    }
}