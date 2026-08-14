plugins {
    id("tamias.java-conventions")
    alias(libs.plugins.shadow)
    application
}

version = "0.1.0"

dependencies {
    implementation(project(":common"))
    implementation(platform(libs.game.bom))
    implementation(platform(libs.falco.bom))
    implementation(libs.adventure)
    implementation(libs.guira)
    implementation(libs.slf4j.api)
    implementation(libs.pica)
    compileOnly(libs.minestom)
    compileOnly(libs.aves)
    compileOnly(libs.xerus)
    compileOnly(libs.falco.anvil)

    // SLF4J needs a binding at runtime; without one it falls back to NOP and the
    // server logs nothing at all.
    runtimeOnly(libs.slf4j.simple)

    // CloudNet is provided by the CloudNet wrapper at runtime and its bridge is loaded as a
    // Minestom extension (separate classloader, see the :bridge module), so :setup neither
    // references nor bundles any CloudNet artifact.
    implementation(platform(libs.minestom.extensions.bom))
    implementation(libs.minestom.extensions)

    // LuckPerms
    implementation(libs.guava)
    compileOnly(libs.luckperms.api) {
        exclude(group = "net.kyori.adventure")
    }
    runtimeOnly(libs.luckperms.minestom.loader) {
        exclude(group = "net.kyori.adventure")
    }

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
