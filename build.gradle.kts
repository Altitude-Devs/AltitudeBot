import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("maven-publish")
}

group = "com.alttd"
version = "1.0.0-SNAPSHOT"
description = "Altitude Discord Bot."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        dependsOn(getByName("relocateJars") as ConfigureShadowRelocation)
        archiveFileName.set("${project.name}-${project.version}.jar")
        minimize()
        configurations = listOf(project.configurations.shadow.get())
    }

    build {
        dependsOn(shadowJar)
    }

    create<ConfigureShadowRelocation>("relocateJars") {
        target = shadowJar.get()
        prefix = "${project.name}.lib"
    }
}

dependencies {
// JDA
    implementation("net.dv8tion:JDA:5.0.0-alpha.3") {
        shadow("net.dv8tion:JDA:5.0.0-alpha.3") {
            exclude("opus-java") // exclude audio
        }
        // MySQL
        runtimeOnly("mysql:mysql-connector-java:8.0.23")
    }

    tasks {

        shadowJar {
            listOf(
                    "net.dv8tion.jda"
            ).forEach { relocate(it, "${rootProject.group}.lib.$it") }
        }

        build {
            dependsOn(shadowJar)
        }
    }
    // Configurate
    shadow("org.spongepowered:configurate-yaml:4.1.2")
}