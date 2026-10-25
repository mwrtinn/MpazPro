package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.mpazpro.data.dao.DesafioDao

class ViewModelFactory(
    private val desafioDao: DesafioDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DocenteViewModel::class.java) -> {
                DocenteViewModel(desafioDao) as T
            }
            modelClass.isAssignableFrom(AlumnoViewModel::class.java) -> {
                AlumnoViewModel(desafioDao) as T
            }
            else -> throw IllegalArgumentException("ViewModel clase desconocida: ${modelClass.name}")
        }
    }
}