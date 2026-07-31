plugins {
    id("tamias.java-conventions")
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.game.bom))
    implementation(libs.atlas.projectiles)
    implementation(libs.adventure)
    implementation(libs.slf4j.api)
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

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
