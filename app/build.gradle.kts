import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Recupera la configuración local sin publicarla en GitHub.
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")

    if (archivo.exists()) {
        archivo.inputStream().use { load(it) }
    }
}

val urlUsuarios = propiedadesLocales.getProperty(
    "MPAZ_URL_USUARIOS",
    "http://10.0.2.2:8081/"
).trimEnd('/') + "/"

val urlAprendizaje = propiedadesLocales.getProperty(
    "MPAZ_URL_APRENDIZAJE",
    "http://10.0.2.2:8082/"
).trimEnd('/') + "/"

val permitirHttpRelease = propiedadesLocales.getProperty(
    "MPAZ_RELEASE_HTTP_LOCAL",
    "false"
).toBoolean()

android {
    namespace = "cl.duoc.mpazpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "cl.duoc.mpazpro"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Direcciones disponibles desde BuildConfig.
        buildConfigField(
            "String",
            "URL_USUARIOS",
            "\"$urlUsuarios\""
        )

        buildConfigField(
            "String",
            "URL_APRENDIZAJE",
            "\"$urlAprendizaje\""
        )

        manifestPlaceholders["mpazCleartext"] = "false"
    }

    buildTypes {

        // Permite HTTP durante las pruebas locales.
        debug {
            manifestPlaceholders["mpazCleartext"] = "true"
        }

        release {
            isMinifyEnabled = false

            // HTTPS por defecto en la versión de entrega.
            manifestPlaceholders["mpazCleartext"] =
                permitirHttpRelease.toString()

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navegación y Lifecycle ViewModel.
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // DataStore Preferences.
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    // Material Icons.
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Room Database.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Comunicación con microservicios y APIs externas.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Carga de imágenes desde Internet.
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Pruebas.
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}