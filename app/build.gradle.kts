import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("jacoco")
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
        applicationId = "cl.mwrtinn.mpazpro"
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

    // Pruebas unitarias de corrutinas.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // Simulación de respuestas HTTP para pruebas.
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // Pruebas.
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Configuración de cobertura de código.
jacoco {
    toolVersion = "0.8.14"
}

// Informe de cobertura de la lógica de la aplicación.
tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>(
    "jacocoDebugReport"
) {

    dependsOn("testDebugUnitTest")

    group = "verification"

    description =
        "Genera el informe de cobertura de la lógica de MPAZ PRO"

    reports {

        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)

        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/logica")
        )

        xml.outputLocation.set(
            layout.buildDirectory.file(
                "reports/jacoco/logica/coverage.xml"
            )
        )
    }

    // Incluye las capas que contienen la lógica del proyecto.
    classDirectories.setFrom(

        fileTree(
            layout.buildDirectory.dir(
                "tmp/kotlin-classes/debug"
            )
        ) {

            include("cl/duoc/mpazpro/utils/**/*.class")
            include("cl/duoc/mpazpro/model/**/*.class")
            include("cl/duoc/mpazpro/viewmodel/**/*.class")
            include("cl/duoc/mpazpro/repository/**/*.class")
            include("cl/duoc/mpazpro/data/remote/**/*.class")
        }
    )

    sourceDirectories.setFrom(
        files("src/main/java", "src/main/kotlin")
    )

    executionData.setFrom(
        layout.buildDirectory.file(
            "jacoco/testDebugUnitTest.exec"
        )
    )
}

// Informe adicional de todo el código Kotlin compilado.
tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>(
    "jacocoCompletoReport"
) {

    dependsOn("testDebugUnitTest")

    group = "verification"

    description =
        "Genera el informe general de cobertura Kotlin"

    reports {

        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)

        html.outputLocation.set(
            layout.buildDirectory.dir(
                "reports/jacoco/completo"
            )
        )

        xml.outputLocation.set(
            layout.buildDirectory.file(
                "reports/jacoco/completo/coverage.xml"
            )
        )
    }

    classDirectories.setFrom(

        fileTree(
            layout.buildDirectory.dir(
                "tmp/kotlin-classes/debug"
            )
        ) {

            include("cl/duoc/mpazpro/**/*.class")

            exclude("**/BuildConfig.class")
            exclude("**/R.class")
            exclude("**/R\$*.class")
            exclude("**/*_Impl.class")
            exclude("**/*_Impl\$*.class")
        }
    )

    sourceDirectories.setFrom(
        files("src/main/java", "src/main/kotlin")
    )

    executionData.setFrom(
        layout.buildDirectory.file(
            "jacoco/testDebugUnitTest.exec"
        )
    )
}