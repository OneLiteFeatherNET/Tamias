plugins {
    id("tamias.java-conventions")
    `java-library`
}

dependencies {
    implementation(platform(libs.game.bom))
    implementation(libs.slf4j.api)
    compileOnly(libs.jetbrains.annotations)

    // Persistence of the yearly statistics. Only opened when stats.jdbc-url is configured, see
    // StatsRepositories. Hibernate runs on a Hikari pool, Flyway owns the schema (the migrations live
    // in this module's resources), PostgreSQL is the production database.
    implementation(libs.hibernate.core)
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)

    // H2 in PostgreSQL mode backs the repository tests.
    testImplementation(libs.h2)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.slf4j.simple)
}
