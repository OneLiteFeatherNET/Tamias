plugins {
    alias(libs.plugins.shadow)
}

group = "net.theevilreaper.tamias.setup"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

    implementation(project(":common"))
    implementation(platform(libs.microtus.bom))
    implementation(platform(libs.dungeon.bom))

    testImplementation(libs.minestom)
    testImplementation(libs.minestom.test)
    testImplementation(libs.aves)
    testImplementation(libs.xerus)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks {
    jar {
        dependsOn("shadowJar")
    }
}
