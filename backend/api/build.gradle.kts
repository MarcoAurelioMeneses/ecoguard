plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom.get().toString())
        mavenBom(libs.spring.ai.bom.get().toString())
    }
}

dependencies {
    implementation(project(":agent"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation(project(":application"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation(libs.springdoc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.postgresql)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":ingestion"))
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.1")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("ecoguard-api.jar")
}

tasks.processResources {
    from(rootProject.file("../db/migration")) {
        into("db/migration")
    }
}
