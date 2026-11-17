# MpazPro - Escuela Marcela Paz 🏫

Proyecto de **Desarrollo de Aplicaciones Móviles (DSY1105)** - Duoc UC, Sede San Bernardo.
Caso: *MPAZ PRO 3.º - Apoyo didáctico y seguimiento del aprendizaje*.

## Contexto 📖
MVP académico para la Escuela Escritora Marcela Paz. Apoya a **Tercero Básico** con contenidos y actividades
organizados por **objetivos de aprendizaje (OA)** y entrega al docente un seguimiento ordenado del avance.
Máximo una unidad por asignatura (Lenguaje, Matemática, Ciencias Naturales, Historia).
**Todos los usuarios, respuestas y resultados son ficticios.**

## Integrantes 👥
- Martín Faúndez
- Joaquín Orellana
- Vicente Vega

## Perfiles y funcionalidades ✅
Los tres perfiles ingresan con **RUT (validado con dígito verificador) y contraseña**.

| Perfil | Qué puede hacer |
|---|---|
| **Estudiante** | Ver los OA asignados con su avance · leer contenidos aprobados (explicación, ejemplo, imagen de apoyo) con **audio de apoyo** · practicar con **desafíos de dificultad progresiva** (retroalimentación inmediata) · rendir la **prueba final** dentro de los intentos permitidos y revisar sus respuestas correctas/incorrectas. **No** puede modificar contenidos ni ver el informe pedagógico. |
| **Docente** | Asignar OA a sus estudiantes · definir los **intentos de la prueba final** · crear contenidos con **imagen de apoyo (cámara o galería)** que quedan pendientes de UTP · crear preguntas (desafío o prueba, nivel 1-3) · ver resultados, **generar el informe pedagógico**, validarlo y compartirlo. |
| **Jefatura UTP** | Revisar los contenidos creados, verificar su OA y **aprobarlos o rechazarlos** (el rechazo exige una observación). Solo los aprobados llegan a los estudiantes. |

El informe pedagógico describe resultados (rendimiento, fortalezas, aspectos a reforzar, fecha y estado de
validación). **No emite diagnósticos ni decisiones académicas automáticas** y requiere validación docente.

## Requisitos de la evaluación (Parcial 2) 📋
| Requisito | Dónde está |
|---|---|
| Interfaz organizada y navegación clara | `navigation/` (rutas tipadas + eventos), `ui/screen/`, barra superior con "Volver", transiciones |
| Formularios validados con íconos y mensajes | Login, contenido, pregunta, intentos y rechazo UTP → `ui/components/CamposFormulario.kt` (ícono inicial, ícono error/válido, `isError` + `supportingText`) |
| Validaciones desde la lógica | `utils/Validaciones.kt` + `validarFormulario()` / `validarContenido()` / `validarPregunta()` en los ViewModels |
| Animaciones y estados | `AnimatedVisibility`, `AnimatedContent`, `animateFloatAsState`, `animateColorAsState`, `derivedStateOf`, `rememberSaveable`, `CircularProgressIndicator` |
| Estructura modular + persistencia local | MVVM + Repository + **Room** (6 tablas) + **DataStore** (RUT recordado); contraseñas con hash SHA-256 |
| ≥ 2 recursos nativos | **Cámara** y **galería** (`SelectorImagen`, FileProvider, permiso en ejecución) · **texto a voz** · **estado de red** · **compartir** informe · respuesta háptica |
| GitHub + Trello | Repositorio y tablero del equipo (ver `PLAN_COMMITS.md`) |

> El caso indica que no se requiere cámara, GPS, micrófono ni datos biométricos. La cámara se usa **solo** para que
> el docente adjunte imágenes de apoyo propias a un contenido (el caso incluye imágenes como recurso didáctico) y
> nunca para fotografiar estudiantes.

## Tecnologías 🛠️
Kotlin · Jetpack Compose · Material 3 · MVVM · Navigation Compose · Room (KSP) · DataStore Preferences ·
Coroutines/StateFlow

## Estructura 📁
```text
cl.duoc.mpazpro/
├── data/local/
│   ├── AppDatabase.kt  PreferenciasDataStore.kt
│   ├── dao/Daos.kt                      # Usuario, Objetivo, Contenido, Pregunta, Respuesta, Informe
│   └── entity/Entidades.kt
├── model/                               # Estados de UI y errores por campo (UiState / Errores)
│   ├── Constantes.kt  LoginUiState.kt  FormulariosDocenteUiState.kt  EstudianteUiState.kt
├── repository/                          # AulaRepository + DatosDePrueba (datos ficticios)
├── utils/                               # Reglas puras sin Android (con pruebas unitarias)
│   ├── Validaciones.kt  Evaluador.kt  GeneradorInforme.kt  Seguridad.kt
├── navigation/                          # Screen (rutas tipadas), NavigationEvent, AppNavigation
├── recursos/                            # Texto a voz, red, compartir, imágenes (FileProvider)
├── ui/
│   ├── components/                      # Campos, login, tarjetas, pestañas docente, UTP, quiz, ImagenInteligente, SelectorImagen
│   ├── screen/                          # Inicio, Login, Estudiante (3), Docente, Utp
│   └── theme/
├── viewmodel/                           # Main (navegación), Login, Estudiante, Docente, Utp + ViewModelFactory
└── MainActivity.kt
```

### Flujo de navegación
Las pantallas no usan el `NavController` directamente: piden ir a otro destino al `MainViewModel`, que emite un
`NavigationEvent`; `AppNavigation` escucha esos eventos y los ejecuta.

## Pasos para ejecutar ▶️
1. Clonar el repositorio y abrirlo con **Android Studio**.
2. Esperar la sincronización de Gradle (requiere internet la primera vez).
3. Elegir un emulador o teléfono **Android 9 (API 28) o superior** y presionar **Run ▶**.
4. Para probar la cámara en el emulador: *Extended controls → Camera*; en un teléfono real funciona directamente.
5. Pruebas unitarias: `./gradlew test`

## Usuarios de prueba (ficticios) 🔑
| Perfil | RUT | Contraseña |
|---|---|---|
| Docente | `12.345.678-5` | `1234` |
| Jefatura UTP | `13.456.789-9` | `1234` |
| Estudiante | `11.111.111-1` | `1234` |
| Estudiante | `22.222.222-2` | `1234` |

## Flujo sugerido para la demostración 🎬
1. **Docente** → *Contenidos* → crear un contenido con una foto o imagen de la galería (queda "Pendiente de UTP").
2. **UTP** → aprobarlo (o rechazarlo con observación).
3. **Estudiante** → Matemática → leer el contenido (🔊) → hacer desafíos → rendir la prueba final.
4. **Docente** → *Resultados* → generar el informe → validarlo → compartirlo.

## Nota sobre el contenido curricular
Los códigos y descripciones de los OA y los contenidos de ejemplo son **propios y de prueba**; deben contrastarse
con el Currículum Nacional (curriculumnacional.mineduc.cl) y ser autorizados por UTP antes de la entrega final.
