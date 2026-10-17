# MpazPro - Escuela Marcela Paz 🏫

Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105) - Duoc UC Sede San Bernardo.

## Contexto del Proyecto 📖
MpazPro es una aplicación móvil desarrollada para la Escuela Marcela Paz. Su objetivo es proporcionar herramientas interactivas para estudiantes de 3° básico y herramientas de gestión para Docentes/UTP. La aplicación integra desafíos matemáticos, validaciones de formularios y captura de evidencias fotográficas, construida bajo estándares modernos de desarrollo Android.

## Integrantes del Equipo 👥
- **Martin Faundez**
- **Joaquin Orellana**
- **Vicente Vega**

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
├── data/                 # Capa de base de datos local (Room)
│   ├── AppDatabase.kt    # Configuración principal de la base de datos (v2)
│   ├── dao/              # Consultas e inserciones a la BD (Data Access Object)
│   │   ├── DocenteDao.kt
│   │   └── EstudianteDao.kt
│   └── entity/           # Tablas de la base de datos
│       ├── DocenteEntity.kt
│       └── EstudianteEntity.kt
│
├── navigation/           # Lógica y rutas de navegación
│   ├── AppNavigation.kt  # Gestor (NavHost) de navegación principal
│   ├── NavigationEvent.kt# Eventos de estado para la navegación
│   └── Screen.kt         # Definición de rutas (Inicio, Login, LoginEstudiante)
│
├── ui/                   # Interfaz de usuario (Jetpack Compose)
│   ├── components/       # Componentes visuales reutilizables (Botones, Tarjetas, etc.)
│   ├── screen/           # Interfaces visuales completas
│   │   ├── InicioScreen.kt          # Pantalla principal de selección de perfil
│   │   ├── LoginScreen.kt           # Pantalla de acceso formal para Docente/UTP
│   │   └── LoginEstudianteScreen.kt # Pantalla de acceso interactivo/amigable para el alumno
│   └── theme/            # Configuración visual global
│       ├── Color.kt      # Paleta de colores de la app (AzulPrincipal, GrisTexto, etc.)
│       ├── Theme.kt      # Tema principal (Modo claro/oscuro de Material 3)
│       └── Type.kt       # Tipografías y estilos de texto
│
├── viewmodel/            # Controladores de la lógica de negocio
│   └── MainViewModel.kt  # Gestión de estados, datos de prueba e inicio de sesión
│
└── MainActivity.kt       # Punto de entrada de la aplicación
