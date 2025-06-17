subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    tasks.withType<JavaCompile> {
        options.release.set(23)
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-Dminestom.inside-test=true")
        finalizedBy(project.tasks.findByPath("jacocoTestReport"))
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<JacocoReport> {
        dependsOn(project.tasks.findByPath("test"))
        reports {
            xml.required.set(true)
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}