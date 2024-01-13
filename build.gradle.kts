
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

    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "${rootProject.group}.${project.name}"
        }
    }

    shadowJar {
        archiveFileName.set(rootProject.name + ".jar")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }

}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.19") {
        exclude("opus-java") // exclude audio
    }
    // MySQL
    implementation("mysql:mysql-connector-java:8.0.33")

    // Configurate
    implementation("org.spongepowered:configurate-yaml:4.1.2")

    // Excel
    implementation("org.apache.poi:poi:5.2.0")
    implementation("org.apache.poi:poi-ooxml:5.2.0")
    // Other stuff?
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("com.alttd:AltitudeLogs:1.0")
}