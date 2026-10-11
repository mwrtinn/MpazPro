# MpazPro - Escuela Marcela Paz 🏫

Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105) - Duoc UC Sede San Bernardo.

## Contexto del Proyecto 📖
MpazPro es una aplicación móvil desarrollada para la Escuela Marcela Paz. Su objetivo es proporcionar herramientas interactivas para estudiantes de 3° básico y herramientas de gestión para Docentes/UTP. La aplicación integra desafíos matemáticos, validaciones de formularios y captura de evidencias fotográficas, construida bajo estándares modernos de desarrollo Android.

## Integrantes del Equipo 👥
* **Martin Faundez
* **Joaquin Orellana
* **Vicente Vega

## Tecnologías Utilizadas 🛠️
* **Lenguaje:** Kotlin
* **Interfaz Gráfica:** Jetpack Compose
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Navegación:** Navigation Compose

## Estructura de Carpetas 📁
El proyecto sigue una arquitectura MVVM limpia, escalable y modularizada según las buenas prácticas:

```text
cl.duoc.mpazpro/
│
├── navigation/           # Lógica y rutas de navegación
│   ├── AppNavigation.kt  # Gestor (NavHost) de navegación principal
│   ├── NavigationEvent.kt# Eventos de estado para la navegación
│   └── Screen.kt         # Definición de rutas selladas seguras
│
├── ui/
│   └── screen/           # Interfaces visuales (Vistas Compose)
│       └── InicioScreen.kt # Pantalla principal de selección de perfil
│
├── viewmodel/            # Controladores de la lógica de negocio
│   └── MainViewModel.kt  # ViewModel principal para gestión de eventos
│
└── MainActivity.kt       # Punto de entrada de la aplicación
