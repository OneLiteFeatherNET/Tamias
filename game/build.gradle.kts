plugins {
    id("tamias.java-conventions")
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.game.bom))
    implementation(platform(libs.falco.bom))
    implementation(libs.atlas.projectiles)
    implementation(libs.adventure)
    implementation(libs.slf4j.api)
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)
    compileOnly(libs.falco.anvil)
    // SLF4J needs a binding at runtime; without one it falls back to NOP and the
    // server logs nothing at all.
    runtimeOnly(libs.slf4j.simple)

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
