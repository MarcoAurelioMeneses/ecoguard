plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.dependency.management) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.spotbugs) apply false
    alias(libs.plugins.errorprone) apply false
    alias(libs.plugins.dependency.check)
    jacoco
}

dependencyCheck {
    failBuildOnCVSS = 9.0f
    formats = listOf("HTML", "JSON")
    outputDirectory.set(layout.buildDirectory.dir("reports/dependency-check"))
    data.directory = "${System.getProperty("user.home")}/.gradle/dependency-check-data"
    val nvdKey = System.getenv("NVD_API_KEY")
    if (!nvdKey.isNullOrBlank()) {
        nvd.apiKey = nvdKey
    }
}

allprojects {
    group = "com.ecoguard"
    version = "0.1.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.spotbugs")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.13"
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    configure<CheckstyleExtension> {
        toolVersion = "10.21.4"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isIgnoreFailures = false
        maxWarnings = 0
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/**/*.java")
            googleJavaFormat("1.28.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        ignoreFailures.set(false)
        toolVersion.set("4.10.5")
        excludeFilter.set(rootProject.file("config/spotbugs/exclude.xml"))
        effort.set(com.github.spotbugs.snom.Effort.DEFAULT)
        reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
    }

    tasks.named("check") {
        dependsOn("spotlessCheck", "checkstyleMain", "checkstyleTest", "spotbugsMain")
    }

    // SpotBugs crashes while analysing some test classes (BCEL vs commons-lang3).
    tasks.matching { it.name == "spotbugsTest" }.configureEach { enabled = false }
    afterEvaluate {
        if (plugins.hasPlugin("org.springframework.boot")) {
            // SpotBugs 4.10 still crashes on some Spring bytecode. Domain and application stay scanned.
            tasks.named("spotbugsMain") { enabled = false }
        }
    }

    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.12.2")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher:1.12.2")
    }
}

// ErrorProne stays optional. Enable with -Perrorprone=true.
if (providers.gradleProperty("errorprone").orNull == "true") {
    configure(listOf(project(":domain"), project(":application"))) {
        apply(plugin = "net.ltgt.errorprone")
        dependencies {
            add("errorprone", libs.errorprone.core)
        }
    }
}
