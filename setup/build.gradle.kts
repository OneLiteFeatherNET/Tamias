group = "net.theevilreaper.tamias.setup"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))

    compileOnly(platform(libs.microtus.bom))
    compileOnly(platform(libs.dungeon.bom))
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)

    testCompileOnly(platform(libs.microtus.bom))
    testCompileOnly(platform(libs.dungeon.bom))
    testImplementation(libs.minestom)
    testImplementation(libs.minestom.test)
    testImplementation(libs.xerus)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
