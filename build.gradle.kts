plugins {
    base
    id("simpletms.root-conventions")
}

val mavenGroup: String by rootProject
val modVersion: String by rootProject

group = mavenGroup
version = modVersion