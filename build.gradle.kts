plugins {
    id("com.gradle.build-scan") version "2.4.2"
    java
    jacoco
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("com.github.kt3k.coveralls") version "2.8.4"
    id("com.palantir.docker-run") version "0.22.1"
    id("org.nosphere.gradle.github.actions") version "1.0.0"
}
apply(from = "gradle/git-version-data.gradle")
apply(from = "gradle/build-scan-data.gradle")

group = "com.github.britter"
version = "0.2.8"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.1.8.RELEASE"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.hsqldb:hsqldb:2.3.3")
    implementation("javax.xml.bind:jaxb-api:2.2.11")
    implementation("javax.validation:validation-api:1.1.0.Final")
    implementation("org.postgresql:postgresql:9.4-1203-jdbc42")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:2.1.8.RELEASE"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("org.assertj:assertj-core:3.13.2")
    testImplementation("org.mockito:mockito-core:3.1.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    processResources {
        filesMatching("**/*.properties") {
            expand(project.properties)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }

    val jacocoTestReport = named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.isEnabled = true
        }
    }

    coveralls {
        jacocoReportPath = jacocoTestReport.map { it.reports.xml.destination }
    }

    bootJar {
        archiveFileName.set("app.jar")
    }

    bootRun {
        if (project.hasProperty("postgres")) {
            setArgsString("--spring.profiles.active=postgres")
        }
    }
}

dockerRun {
    image = "postgres:9.4.4"
    name = "postgres-db"
    ports("5432:5432")
    env(mapOf(
        "POSTGRES_PASSWORD" to "spring-boot-heroku-example",
        "POSTGRES_USER" to "spring-boot-heroku-example"
    ))
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}
