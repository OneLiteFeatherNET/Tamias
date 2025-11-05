plugins {
    alias(libs.plugins.cyclonedx)
}
subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    tasks.withType<JavaCompile> {
        options.release.set(25)
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-Dminestom.inside-test=true")
        finalizedBy("jacocoTestReport")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<JacocoReport> {
        dependsOn("test")
        reports {
            xml.required.set(true)
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }
}