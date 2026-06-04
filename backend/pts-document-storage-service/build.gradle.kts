plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("jacoco")
}

group = "${rootProject.group}.document.storage"
version = rootProject.version

dependencies {
    implementation(rootProject.extra["springBootStarterLogging"] as String)
    implementation(rootProject.extra["s3"] as String)

    implementation(rootProject.extra["springBootStarterWeb"] as String)
    implementation(rootProject.extra["springBootStarterActuator"] as String)
    implementation(rootProject.extra["springBootStarterLogging"] as String)
    implementation(rootProject.extra["springBootStarterValidation"] as String)
    implementation(rootProject.extra["springBootStarterOauth2"] as String)
    implementation(rootProject.extra["springBootStarterSecurity"] as String)
    developmentOnly(rootProject.extra["springBootDevtools"] as String)

    implementation(rootProject.extra["redis"] as String)

    implementation(rootProject.extra["bucket4jCore"] as String)
    implementation(rootProject.extra["bucket4jLettuce"] as String)

    implementation(rootProject.extra["lombok"] as String)
    compileOnly(rootProject.extra["lombok"] as String)
    annotationProcessor(rootProject.extra["lombok"] as String)

//    implementation(rootProject.extra["springBootStarterDataJpa"] as String)
//    implementation(rootProject.extra["springBootStarterDataJdbc"] as String)
//    implementation(rootProject.extra["springBootStarterJdbc"] as String)
//    implementation(rootProject.extra["postgresql"] as String)
//    runtimeOnly(rootProject.extra["postgresql"] as String)

    implementation(rootProject.extra["lombok"] as String)
    compileOnly(rootProject.extra["lombok"] as String)
    annotationProcessor(rootProject.extra["lombok"] as String)

    developmentOnly(rootProject.extra["springBootDevtools"] as String)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.pts.document.storage.DocumentStorageApplication")
}

tasks.register("serviceInfo") {
    doLast {
        println("Module: pts-document-storage-service")
        println("Group: ${project.group}, Version: ${project.version}")
    }
}
