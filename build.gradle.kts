plugins {
  `java-library`
  `maven-publish`
  id("org.sonarqube") version "2.7.1"
  jacoco
}

group = "de.scaramanga"
version = "0.1.1"

repositories {
  mavenCentral()
  jcenter()
}

dependencies {

  api("org.springframework.boot:spring-boot-starter:2.1.8.RELEASE")
  api("net.dv8tion:JDA:4.0.0_46") {
    exclude(module = "opus-java")
  }

  implementation("org.reflections:reflections:0.9.11")

  lombok("1.18.8")

  val jUnitVersion = "5.5.2"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.8.RELEASE") {
    exclude(module = "junit")
    exclude(module = "hamcrest-library")
    exclude(module = "hamcrest-core")
    exclude(module = "json-path")
    exclude(module = "jsonassert")
    exclude(module = "xmlunit-core")
  }

  testImplementation("org.assertj:assertj-core:3.13.2")
  testImplementation("org.awaitility:awaitility:4.0.1")
}

tasks.withType(Test::class) {
  useJUnitPlatform()
  finalizedBy("jacocoTestReport")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
  main {
    resources {
      include {
        listOf("")
            .contains(it.name)
      }
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("Lily") {

      artifactId = "lily"
      from(components["java"])
    }
  }
}

sonarqube {

  val sonarLogin: String by project

  properties {
    property("sonar.projectKey", "scaramangado_lily")
    property("sonar.organization", "scaramangado")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.login", sonarLogin)
  }
}

fun DependencyHandler.lombok(version: Any) {

  val dependencyNotation = "org.projectlombok:lombok:$version"

  implementation(dependencyNotation)
  testImplementation(dependencyNotation)

  annotationProcessor(dependencyNotation)
  testAnnotationProcessor(dependencyNotation)
}

tasks.withType(Wrapper::class) {
  gradleVersion = "5.6.2"
}
