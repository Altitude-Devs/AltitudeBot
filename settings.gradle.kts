rootProject.name = "AltitudeBot"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        // JDA
        maven("https://m2.dv8tion.net/releases/")
        // Configurate
        maven("https://repo.spongepowered.org/maven")
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
