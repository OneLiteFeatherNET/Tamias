plugins {
    id("tamias.java-conventions")
}

dependencies {
    implementation(platform(libs.game.bom))
    implementation(platform(libs.falco.bom))
    implementation(libs.adventure)
    implementation(libs.minestom)
    implementation(libs.aves)
    implementation(libs.xerus)
    implementation(libs.falco.anvil)
    implementation(libs.slf4j.api)

    compileOnly(libs.luckperms.api) {
        exclude(group = "net.kyori.adventure")
    }
    // Only to compile LuckPermsSupport.bootstrap(). The artifact is shipped by :game and :setup as
    // runtimeOnly - common must not put it on any runtime class path, because its absence is exactly
    // what LuckPermsSupport detects.
    compileOnly(libs.luckperms.minestom.loader) {
        exclude(group = "net.kyori.adventure")
    }

    testImplementation(libs.minestom)
    testImplementation(libs.luckperms.api) {
        exclude(group = "net.kyori.adventure")
    }

    testImplementation(libs.minestom)
    testImplementation(libs.cyano)
    testImplementation(libs.aves)
    testImplementation(libs.xerus)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.engine)
}