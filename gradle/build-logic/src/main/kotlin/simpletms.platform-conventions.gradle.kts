plugins {
    id("simpletms.base-conventions")
    id("com.github.johnrengelman.shadow")
}

val bundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(project(":common", configuration = "namedElements")) { isTransitive = false }
}

tasks {
    jar {
        archiveBaseName.set("SimpleTMs-${project.name}")
        archiveClassifier.set("dev")
    }

    shadowJar {
        exclude("architectury.common.json")

        archiveBaseName.set("SimpleTMs-${project.name}")
        archiveClassifier.set("dev-shadow")
        configurations = listOf(bundle)
        mergeServiceFiles()
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })

        archiveBaseName.set("SimpleTMs-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }
}
