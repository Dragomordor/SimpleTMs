import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    java
    `java-library`
    kotlin("jvm")
    id("architectury-plugin")
    id("dev.architectury.loom")
}

repositories {
    maven("https://maven.impactdev.net/repository/development/")
    // maven("https://maven.neoforged.net/releases")
    maven("https://maven.minecraftforge.net/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
//    compilerOptions {
//        apiVersion.set(KotlinVersion.KOTLIN_2_0)
//        jvmTarget.set(JvmTarget.JVM_17)
//    }
}

architectury {
    minecraft = libs.minecraft.get().version!!
}

@Suppress("UnstableApiUsage")
loom {
    silentMojangMappingsLicense()
    mixin {
        useLegacyMixinAp.set(true)
        defaultRefmapName.set("simpletms-${project.name}-refmap.json")
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

//    testRuntimeOnly(libs.junit.launcher)
//    testImplementation(libs.junit.engine)
//    testImplementation(libs.mockk)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        maxHeapSize = "1G"
    }

    withType<JavaCompile> {
        options.release.set(17)
    }

    withType<Jar> {
        from(rootProject.file("LICENSE"))
    }
}
