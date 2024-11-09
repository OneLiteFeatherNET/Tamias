
group = "net.theevilreaper.tamias.common"
version = "1.0-SNAPSHOT"

dependencies {
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