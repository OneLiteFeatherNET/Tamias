
group = "net.theevilreaper.tamias.common"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(platform(libs.mycelium.bom))
    implementation(platform(libs.game.bom))
    implementation(libs.adventure)
    implementation(libs.minestom)
    implementation(libs.aves)
    implementation(libs.xerus)

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