import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI
import kotlin.collections.setOf

plugins {
    kotlin("jvm") version "1.3.30"
    id("com.github.johnrengelman.shadow") version "2.0.4"
    application
}

group = "com.namazed.tamtambot"
version = "0.0.1"

val ktor_version = "1.1.3"

repositories {
    mavenCentral()
    maven { url = URI("http://dl.bintray.com/kotlin/kotlin-eap/") }
    maven { url = URI("http://dl.bintray.com/kotlin/ktor") }
    maven { url = URI("https://dl.bintray.com/kotlin/kotlinx") }
    maven { url = URI("https://plugins.gradle.org/m2/") }
    jcenter()
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("ch.qos.logback:logback-classic:1.2.1")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("org.koin:koin-ktor:1.0.2")
    implementation("com.h2database:h2:1.4.197")
    implementation("org.jetbrains.exposed:exposed:0.11.2")
    implementation("com.zaxxer:HikariCP:2.7.8")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.0")
    implementation("com.github.Namazed:TamTamBotApiClientDsl:0.3.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-XXLanguage:+InlineClasses"
        freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlin.Experimental"
    }
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<ShadowJar> {
    baseName = "tamtam-orthobot"
    classifier = ""
    version = ""
}
