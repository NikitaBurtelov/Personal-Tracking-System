plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("jacoco")
}

group = "${rootProject.group}.document.storage"
version = rootProject.version

dependencies {
    implementation(libs.spring.web)
    implementation(libs.spring.amqp)
    implementation(libs.spring.doc)
    implementation(libs.spring.actuator)
    implementation(libs.spring.validation)
    implementation(libs.spring.jdbc)
    implementation(libs.spring.data.jpa)
    implementation(libs.spring.data.jdbc)

    implementation(libs.infra.liquibase)

    implementation(libs.util.lombok)
    compileOnly(libs.util.lombok)
    annotationProcessor(libs.util.lombok)

    implementation(libs.infra.awssdk.s3)
    implementation(libs.infra.awssdk.client.apache)
    implementation(libs.infra.awssdk.client.spi)
    runtimeOnly(libs.infra.postgresql)

    developmentOnly(libs.spring.devtools)

    testImplementation(libs.spring.test)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.pts.document.storage.DocumentStorageApplication")
}

tasks.register("serviceInfo") {
    description = ""
    doLast {
        println("Module: document-storage-service")
        println("Group: ${project.group}, Version: ${project.version}")
    }
}