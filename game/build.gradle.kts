plugins {
    alias(libs.plugins.shadow)
    application
}

group = "net.theevilreaper.tamias.game"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.mycelium.bom))
    implementation(platform(libs.game.bom))
    implementation(libs.atlas.projectiles)
    implementation(libs.adventure)
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

    testImplementation(platform(libs.mycelium.bom))
    testImplementation(platform(libs.game.bom))
    testImplementation(libs.minestom)
    testImplementation(libs.cyano)
    testImplementation(libs.aves)
    testImplementation(libs.xerus)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.engine)
}

application {
    mainClass.set("net.theevilreaper.tamias.TamiasLauncher")
}

tasks {
    jar {
        dependsOn("shadowJar")
    }
}
