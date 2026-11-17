package cl.duoc.mpazpro.utils

/**
 * Reglas de validación de los formularios, centralizadas y sin dependencias de Android ni de Compose.
 *
 * Cada función devuelve el **mensaje de error** o `null` cuando el valor es válido. Los ViewModels
 * llaman a estas funciones y la interfaz solo muestra el mensaje bajo el campo (isError + supportingText).
 */
object Validaciones {

    // ---------- RUT ----------

    /** Quita puntos, guiones y espacios y deja la K en mayúscula: "11.111.111-1" -> "111111111". */
    fun normalizarRut(raw: String): String = raw.filter { it.isLetterOrDigit() }.uppercase()

    /** Calcula el dígito verificador de un RUT por el método del módulo 11. */
    fun calcularDv(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val r = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> '0' + r
        }
    }

    fun validarRut(raw: String): String? {
        val rut = normalizarRut(raw)
        if (rut.isEmpty()) return "El RUT es obligatorio"
        if (rut.length !in 8..9) return "El RUT debe tener entre 8 y 9 caracteres"
        val cuerpo = rut.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return "Solo números y un dígito verificador (0-9 o K)"
        if (calcularDv(cuerpo) != rut.last()) return "El RUT no es válido (revisa el dígito verificador)"
        return null
    }

    fun validarContrasena(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 4 -> "Mínimo 4 caracteres"
        else -> null
    }

    // ---------- Contenido ----------

    fun validarTitulo(valor: String): String? = texto(valor, "El título", 3, 60)
    fun validarExplicacion(valor: String): String? = texto(valor, "La explicación", 20, 400)
    fun validarEjemplo(valor: String): String? = texto(valor, "El ejemplo", 5, 200)

    // ---------- Pregunta ----------

    fun validarEnunciado(valor: String): String? = texto(valor, "El enunciado", 10, 250)
    fun validarAlternativa(valor: String): String? = texto(valor, "La alternativa", 1, 60)

    /** Devuelve un error si [valor] es igual (sin importar mayúsculas) a alguna de las [anteriores]. */
    fun alternativaRepetida(valor: String, anteriores: List<String>): String? {
        val v = valor.trim().lowercase()
        return if (v.isNotEmpty() && anteriores.any { it.trim().lowercase() == v }) "Debe ser distinta de las otras alternativas" else null
    }

    // ---------- Diálogos ----------

    fun validarIntentos(valor: String): String? = entero(valor, "Los intentos", 1, 3)
    fun validarObservacion(valor: String): String? = texto(valor, "La observación", 5, 200)

    // ---------- Utilidades privadas ----------

    private fun texto(valor: String, nombre: String, min: Int, max: Int): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "$nombre es obligatorio"
            v.length < min -> "$nombre debe tener al menos $min caracteres"
            v.length > max -> "$nombre no puede superar los $max caracteres"
            else -> null
        }
    }

    private fun entero(valor: String, nombre: String, min: Int, max: Int): String? {
        val n = valor.trim().toIntOrNull() ?: return "$nombre debe ser un número"
        return if (n !in min..max) "$nombre debe estar entre $min y $max" else null
    }
}
