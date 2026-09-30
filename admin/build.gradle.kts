import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Herramienta de línea de comandos para cargar resultados en Firestore (usa el Admin SDK).
// Uso: ./gradlew -q :admin:run --args="ayuda"
plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("com.jluna.revisarjugadas.admin.MainKt")
    // Acentos bien en la consola de Windows y sin avisos de acceso nativo (lo usa una librería de Firebase)
    applicationDefaultJvmArgs = listOf(
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8",
        "--enable-native-access=ALL-UNNAMED",
        "--sun-misc-unsafe-memory-access=allow",
    )
}

// Las rutas relativas (como la de las credenciales) se toman desde la raíz del proyecto
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":dominio"))
    implementation(libs.firebase.admin)
    implementation(libs.clikt)
    runtimeOnly(libs.slf4j.simple)
}
