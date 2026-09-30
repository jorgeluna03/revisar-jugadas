import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Lógica de negocio pura (sin Android ni Firebase): la usan la app y la herramienta de admin
plugins {
    alias(libs.plugins.kotlin.jvm)
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

dependencies {
    testImplementation(libs.junit)
}
