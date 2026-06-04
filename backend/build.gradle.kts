plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    kotlin("plugin.allopen") version "2.1.21"
    kotlin("plugin.noarg") version "2.1.21"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("java")
    id("jacoco")
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}
group = "org.pts"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

extra.apply {
    set("kotlinStdlib", "org.jetbrains.kotlin:kotlin-stdlib")
    set("kotlinReflect", "org.jetbrains.kotlin:kotlin-reflect")
    set("kotlinCoroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    set("springBootStarterActuator", "org.springframework.boot:spring-boot-starter-actuator")
    set("springBootStarterWeb", "org.springframework.boot:spring-boot-starter-web")
    set("springBootStarterDataJpa", "org.springframework.boot:spring-boot-starter-data-jpa")
    set("springBootStarterDataJdbc", "org.springframework.boot:spring-boot-starter-data-jdbc")
    set("springBootStarterJdbc", "org.springframework.boot:spring-boot-starter-jdbc")
    set("springBootStarterLogging", "org.springframework.boot:spring-boot-starter-logging")
    set("springBootStarterSecurity", "org.springframework.boot:spring-boot-starter-security")
    set("springBootStarterValidation", "org.springframework.boot:spring-boot-starter-validation")
    set("springBootDevtools", "org.springframework.boot:spring-boot-devtools")
    set("springBootStarterOauth2", "org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    set("micrometer", "io.micrometer:micrometer-registry-prometheus:1.16.3")

    set("postgresql", "org.postgresql:postgresql:42.7.3")
    set("redis", "org.springframework.boot:spring-boot-starter-data-redis")

    set("bucket4jCore", "com.bucket4j:bucket4j_jdk17-core:8.18.0")
    set("bucket4jLettuce", "com.bucket4j:bucket4j_jdk17-lettuce:8.18.0")

    set("jackson", "com.fasterxml.jackson.module:jackson-module-kotlin")

    set("lombok", "org.projectlombok:lombok")

    set("jjwtApi", "io.jsonwebtoken:jjwt-api:0.11.5")
    set("jjwtImpl", "io.jsonwebtoken:jjwt-impl:0.11.5")
    set("jjwtJackson", "io.jsonwebtoken:jjwt-jackson:0.11.5")

    set("dotenv", "io.github.cdimascio:dotenv-kotlin:6.4.1")
    set("jsoup", "org.jsoup:jsoup:1.17.2")

    set("s3", "software.amazon.awssdk:s3:2.40.2")

    set("springKafka", "org.springframework.kafka:spring-kafka")
    set("springExpression", "org.springframework:spring-expression")
    set("springRetry", "org.springframework.retry:spring-retry:2.0.3")
    set("springDoc", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    set("springDocReact", "org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.9")
    set("kotlinLogging", "io.github.microutils:kotlin-logging-jvm:3.0.5")

    set("springBootStarterTest", "org.springframework.boot:spring-boot-starter-test")
    set("junitJupiter", "org.junit.jupiter:junit-jupiter:5.11.0")
    set("mockitoCore", "org.mockito:mockito-core:5.12.0")
    set("mockitoKotlin", "org.mockito.kotlin:mockito-kotlin:5.3.1")

    //React stack
    set("SpringBootStarterWebFlux", "org.springframework.boot:spring-boot-starter-webflux")
    set("SpringBootStarterR2dbc", "org.springframework.boot:spring-boot-starter-data-r2dbc")
    set("r2dbcPostgresql", "org.postgresql:r2dbc-postgresql")
    set("reactKafka", "io.projectreactor.kafka:reactor-kafka:1.3.25")
}

configure(subprojects) {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }

    apply(plugin = "org.springframework.boot")
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "jacoco")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin-spring")
    apply(plugin = "java-library")

    dependencies {
        implementation(rootProject.extra["kotlinStdlib"] as String)
        implementation(rootProject.extra["kotlinReflect"] as String)

        implementation(rootProject.extra["springBootStarterActuator"] as String)
        implementation(rootProject.extra["springBootStarterLogging"] as String)

        implementation(rootProject.extra["micrometer"] as String)
        implementation(rootProject.extra["dotenv"] as String)


        implementation(rootProject.extra["springDoc"] as String)

        implementation(rootProject.extra["kotlinLogging"] as String)

        testImplementation(rootProject.extra["springBootStarterTest"] as String)
        testImplementation(rootProject.extra["junitJupiter"] as String)
        testImplementation(rootProject.extra["mockitoCore"] as String)
        testImplementation(rootProject.extra["mockitoKotlin"] as String)
    }
}