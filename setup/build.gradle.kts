plugins {
    alias(libs.plugins.shadow)
    application
}

group = "net.theevilreaper.tamias"
version = "0.1.0"

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.game.bom))
    implementation(libs.adventure)
    implementation(libs.guira)
    implementation(libs.pica)
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

    testImplementation(project(":common"))
    testImplementation(platform(libs.game.bom))
    testImplementation(libs.minestom)
    testImplementation(libs.aves)
    testImplementation(libs.cyano)
    testImplementation(libs.adventure)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.engine)
}

application {
    mainClass.set("net.theevilreaper.tamias.setup.SetupServer")
}

tasks {
    jar {
        dependsOn("shadowJar")
    }
}
