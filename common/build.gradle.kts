plugins {
    id("tamias.java-conventions")
}

dependencies {
    implementation(platform(libs.game.bom))
    implementation(libs.adventure)
    implementation(libs.minestom)
    implementation(libs.aves)
    implementation(libs.xerus)
    implementation(libs.slf4j.api)

    testImplementation(libs.minestom)
    testImplementation(libs.cyano)
    testImplementation(libs.aves)
    testImplementation(libs.xerus)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.engine)
}