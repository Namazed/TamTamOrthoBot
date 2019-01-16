import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
    application
}

group = "com.namazed.tamtambot"
version = "0.0.1"

val ktor_version = "1.1.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("ch.qos.logback:logback-classic:1.2.1")
    compile("io.ktor:ktor-client-okhttp:$ktor_version")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("io.ktor:ktor-client-okhttp:$ktor_version")
    compile("io.ktor:ktor-client-gson:$ktor_version")
    compile("io.ktor:ktor-gson:$ktor_version")
    compile("org.koin:koin-ktor:1.0.2")
    compile("com.h2database:h2:1.4.197")
    compile("org.jetbrains.exposed:exposed:0.11.2")
    compile("com.zaxxer:HikariCP:2.7.8")
    compile("com.squareup.okhttp3:logging-interceptor:+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "1.8"
        freeCompilerArgs += "-XXLanguage:+InlineClasses"
    }
}
