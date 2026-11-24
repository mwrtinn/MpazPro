package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.data.local.PreferenciasDataStore
import cl.duoc.mpazpro.repository.AulaRepository
import cl.duoc.mpazpro.repository.AulaRemotaRepository

// Centraliza las dependencias de los ViewModels.
class ViewModelFactory(
    database: AppDatabase,
    private val preferencias: PreferenciasDataStore
) : ViewModelProvider.Factory {

    // Repositorio local y autenticación.
    private val repository = AulaRepository(
        usuarios = database.usuarioDao(),
        objetivos = database.objetivoDao(),
        contenidos = database.contenidoDao(),
        preguntas = database.preguntaDao(),
        respuestas = database.respuestaDao(),
        informes = database.informeDao()
    )

    // Repositorio conectado con ambos microservicios.
    private val repositoryRemoto by lazy {
        AulaRemotaRepository()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val viewModel: ViewModel = when {

            modelClass.isAssignableFrom(
                MainViewModel::class.java
            ) -> MainViewModel()

            modelClass.isAssignableFrom(
                LoginViewModel::class.java
            ) -> LoginViewModel(
                repository,
                preferencias
            )

            modelClass.isAssignableFrom(
                EstudianteViewModel::class.java
            ) -> EstudianteViewModel(repositoryRemoto)

            modelClass.isAssignableFrom(
                DocenteViewModel::class.java
            ) -> DocenteViewModel(repositoryRemoto)

            modelClass.isAssignableFrom(
                UtpViewModel::class.java
            ) -> UtpViewModel(repositoryRemoto)

            else -> throw IllegalArgumentException(
                "ViewModel clase desconocida: ${modelClass.name}"
            )
        }

        return viewModel as T
    }
}