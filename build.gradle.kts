plugins {
  `java-library`
  `maven-publish`
  id("org.sonarqube") version "2.7.1"
  jacoco
}

group = "de.scaramangado"
version = "0.2.2"

repositories {
  mavenCentral()
  maven("dv8tion") {
    url = uri("https://m2.dv8tion.net/releases")
  }
}

dependencies {

  api("org.springframework.boot:spring-boot-starter:2.7.14")
  api("net.dv8tion:JDA:4.4.1_353") {
    exclude(module = "opus-java")
  }

  implementation("org.reflections:reflections:0.9.11") // Breaking change in 0.9.12

  lombok("1.18.28")

  val jUnitVersion = "5.10.0"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.14") {
    exclude(module = "junit")
    exclude(module = "hamcrest-library")
    exclude(module = "hamcrest-core")
    exclude(module = "json-path")
    exclude(module = "jsonassert")
    exclude(module = "xmlunit-core")
    exclude(group = "org.junit.vintage")
  }

  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.withType(Test::class) {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed")
  }

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

  val githubPackagesUser: String by project
  val githubPackagesToken: String by project

  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/scaramangado/lily")
      credentials {
        username = githubPackagesUser
        password = githubPackagesToken
      }
    }
  }

  publications {
    create<MavenPublication>("Lily") {

      artifactId = "lily"
      from(components["java"])
    }
  }
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
  }
}

sonarqube {

  val sonarUsername: String by project
  val sonarPassword: String by project

  properties {
    property("sonar.projectKey", "scaramangado_lily")
    property("sonar.organization", sonarUsername)
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.login", sonarPassword)
    property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/test/jacocoTestReport.xml")
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
  gradleVersion = "6.5.1"
}
