plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("jacoco")
}

group = "${rootProject.group}.document.storage"
version = rootProject.version

dependencies {
    implementation(libs.spring.doc)
    implementation(libs.spring.web.flux)
    implementation(libs.spring.config)
    implementation(libs.spring.kafka)
    implementation(libs.spring.actuator)
    implementation(libs.spring.validation)
    implementation(libs.spring.jdbc)
    implementation(libs.spring.data.jpa)
    implementation(libs.spring.data.jdbc)

    implementation(libs.infra.liquibase)

    implementation(libs.util.jackson)
    compileOnly(libs.util.lombok)
    annotationProcessor(libs.util.lombok)

    implementation(libs.infra.awssdk.s3)
    implementation(libs.infra.awssdk.client.apache)
    implementation(libs.infra.awssdk.client.spi)
    runtimeOnly(libs.infra.postgresql)

    developmentOnly(libs.spring.devtools)

    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry)

    testImplementation(libs.spring.test)
    testCompileOnly(libs.util.lombok)
    testAnnotationProcessor(libs.util.lombok)

    testImplementation(libs.spring.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.minio)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.pts.document.storage.DocumentStorageApplication")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("serviceInfo") {
    description = ""
    doLast {
        println("Module: document-storage-service")
        println("Group: ${project.group}, Version: ${project.version}")
    }
}