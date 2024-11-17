plugins {
    alias(libs.plugins.shadow)
}

group = "net.theevilreaper.tamias.game"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.microtus.bom))
    implementation(platform(libs.dungeon.bom))

    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

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
