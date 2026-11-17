package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.data.local.PreferenciasDataStore
import cl.duoc.mpazpro.repository.AulaRepository

/**
 * Fábrica central de ViewModels: es el único lugar donde se arman sus dependencias
 * (repositorio y DataStore), por lo que los ViewModels no conocen Room ni el Context.
 */
class ViewModelFactory(
    database: AppDatabase,
    private val preferencias: PreferenciasDataStore
) : ViewModelProvider.Factory {

    private val repository = AulaRepository(
        usuarios = database.usuarioDao(),
        objetivos = database.objetivoDao(),
        contenidos = database.contenidoDao(),
        preguntas = database.preguntaDao(),
        respuestas = database.respuestaDao(),
        informes = database.informeDao()
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel()
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository, preferencias)
            modelClass.isAssignableFrom(EstudianteViewModel::class.java) -> EstudianteViewModel(repository)
            modelClass.isAssignableFrom(DocenteViewModel::class.java) -> DocenteViewModel(repository)
            modelClass.isAssignableFrom(UtpViewModel::class.java) -> UtpViewModel(repository)
            else -> throw IllegalArgumentException("ViewModel clase desconocida: ${modelClass.name}")
        }
        return viewModel as T
    }
}
