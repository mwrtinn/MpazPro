# MpazPro - Escuela Marcela Paz 🏫

Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105) - Duoc UC Sede San Bernardo.

## Contexto del Proyecto 📖
MpazPro es una aplicación móvil desarrollada para la Escuela Marcela Paz. Su objetivo es proporcionar herramientas interactivas para estudiantes de 3° básico y herramientas de gestión para Docentes/UTP. La aplicación integra desafíos matemáticos, validaciones de formularios y captura de evidencias fotográficas, construida bajo estándares modernos de desarrollo Android.

## Integrantes del Equipo 👥
- **Martin Faundez*
- **Joaquin Orellana*
- **Vicente Vega*

## Tecnologías Utilizadas 🛠️
- **Lenguaje:** Kotlin
- **Interfaz Gráfica:** Jetpack Compose
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Navegación:** Navigation Compose
- **Base de Datos Local:** Room Database

## Estructura de Carpetas 📁
El proyecto sigue una arquitectura MVVM limpia, escalable y modularizada según las buenas prácticas:

```text
cl.duoc.mpazpro/
│
├── data/                    # Capa de base de datos local (Room)
│   ├── AppDatabase.kt       # Configuración principal de la base de datos (v4)
│   ├── dao/                 # Consultas e inserciones a la BD (Data Access Object)
│   │   ├── DesafioDao.kt
│   │   ├── DocenteDao.kt
│   │   ├── EstudianteDao.kt
│   │   └── RegistroActividadDao.kt
│   └── entity/              # Tablas de la base de datos
│       ├── DesafioEntity.kt
│       ├── DocenteEntity.kt
│       ├── EstudianteEntity.kt
│       └── RegistroActividadEntity.kt
│
├── navigation/              # Lógica y rutas de navegación
│   ├── AppNavigation.kt     # Gestor (NavHost) de navegación principal
│   ├── NavigationEvent.kt   # Eventos de estado para la navegación
│   └── Screen.kt            # Definición de rutas (Inicio, Login, Perfiles)
│
├── ui/                      # Interfaz de usuario (Jetpack Compose)
│   ├── components/          # Componentes visuales reutilizables (Botones, Tarjetas, etc.)
│   ├── screen/              # Interfaces visuales completas
│   │   ├── AlumnoScreen.kt          # Vista y portal del estudiante
│   │   ├── DocenteScreen.kt         # Vista y panel de control del docente
│   │   ├── InicioScreen.kt          # Pantalla principal de selección de perfil
│   │   ├── LoginScreen.kt           # Pantalla de acceso formal para Docentes
│   │   └── LoginEstudianteScreen.kt # Pantalla de acceso amigable para el alumno
│   └── theme/               # Configuración visual global
│       ├── Color.kt         # Paleta de colores de la app (Material 3)
│       ├── Theme.kt         # Tema principal (Modo claro/oscuro)
│       └── Type.kt          # Tipografías y estilos de texto
│
├── viewmodel/               # Controladores de la lógica de negocio y arquitectura MVVM
│   ├── AlumnoViewModel.kt   # Gestión de datos y estados para el estudiante
│   ├── DocenteViewModel.kt  # Gestión de datos y estados para el docente
│   ├── MainViewModel.kt     # Gestión de accesos, datos de prueba y sesión global
│   └── ViewModelFactory.kt  # Fábrica centralizada para inyección de dependencias DAO
│
└── MainActivity.kt          # Punto de entrada de la aplicación y configuración de vistas
