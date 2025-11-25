configurations.all {
    resolutionStrategy {
        force(libs.fabric.loader)
    }
}

plugins {
    id("simpletms.platform-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

dependencies {
    "developmentFabric"(project(":common", configuration = "namedElements")) { isTransitive = false }
    bundle(project(":common", configuration = "transformProductionFabric")) { isTransitive = false }

    modApi(libs.architectury.fabric)

    modImplementation(libs.fabric.loader)
    modApi(libs.fabric.api)
    modApi(libs.fabric.kotlin)

//	Uncomment the following line to enable the deprecated Fabric API modules.
//	These are included in the Fabric API production distribution and allow you to update your mod to the latest modules at a later more convenient time.
//    modImplementation(libs.fabric.apiDeprecated)

    modImplementation(libs.cobblemon.fabric)
}

tasks {
    processResources {
        val minecraftVersion = libs.minecraft.get().version.toString()
        val loaderVersion = libs.fabric.loader.get().version.toString()
        val architecturyVersion = libs.architectury.fabric.get().version?.split('.')?.get(0)
        val cobblemonVersion = libs.cobblemon.fabric.get().version?.split('+')?.get(0)

        inputs.property("minecraft_version", libs.minecraft.get().version)
        inputs.property("loader_version", libs.fabric.loader.get().version)
        inputs.property("architectury_version", architecturyVersion)
        inputs.property("cobblemon_version", cobblemonVersion)
        inputs.property("version", rootProject.version)

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to rootProject.version.toString(),
                    "architectury_version" to architecturyVersion,
                    "loader_version" to loaderVersion,
                    "minecraft_version" to minecraftVersion,
                    "cobblemon_version" to cobblemonVersion,
                )
            )
        }
    }

}
