plugins {
    id("simpletms.platform-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    "developmentNeoForge"(project(":common", configuration = "namedElements")) { isTransitive = false }
    bundle(project(":common", configuration = "transformProductionNeoForge")) { isTransitive = false }

    modApi(libs.architectury.neoforge)

    neoForge(libs.neoforge.loader)
    implementation(libs.neoforge.kotlin) {
        exclude("net.neoforged.fancymodloader", "loader")
    }

    modImplementation(libs.cobblemon.neoforge)
}

tasks {
    processResources {
        val neoforgeVersion = libs.neoforge.loader.get().version?.split('.')?.get(0)
        val forgeKotlinVersion = libs.neoforge.kotlin.get().version?.split('.')?.get(0)
        val architecturyVersion = libs.architectury.neoforge.get().version?.split('.')?.get(0)
        val cobblemonVersion = libs.cobblemon.neoforge.get().version?.split('+')?.get(0)

        inputs.property("minecraft_version", libs.minecraft.get().version)
        inputs.property("loader_version", neoforgeVersion)
        inputs.property("forge_kotlin_version", forgeKotlinVersion)
        inputs.property("architectury_version", architecturyVersion)
        inputs.property("cobblemon_version", cobblemonVersion)
        inputs.property("version", rootProject.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(
                "version" to rootProject.version,
                "architectury_version" to architecturyVersion as Any,
                "forge_kotlin_version" to forgeKotlinVersion as Any,
                "loader_version" to neoforgeVersion as Any,
                "minecraft_version" to libs.minecraft.get().version as Any,
                "cobblemon_version" to cobblemonVersion as Any,
            )
        }
    }
}
