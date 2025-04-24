plugins {
    alias(libs.plugins.shadow)
}

group = "net.theevilreaper.tamias.setup"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.microtus.bom))
    implementation(platform(libs.mycelium.bom))
    implementation(platform(libs.game.bom))

    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

    testImplementation(libs.minestom)
    testImplementation(libs.minestom.test)
    testImplementation(libs.game.bom)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks {
    jar {
        dependsOn("shadowJar")
    }
}
