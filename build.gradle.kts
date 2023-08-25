plugins {
  `java-library`
  `maven-publish`
}

group = "de.scaramangado"
version = "0.3.0"

repositories {
  mavenCentral()
  maven("dv8tion") {
    url = uri("https://m2.dv8tion.net/releases")
  }
}

dependencies {

  api("org.springframework.boot:spring-boot-starter:3.1.3")
  api("net.dv8tion:JDA:4.4.1_353") {
    exclude(module = "opus-java")
  }

  implementation("org.reflections:reflections:0.10.2") // Breaking change in 0.9.12

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

tasks.withType<Test> {
  useJUnitPlatform()
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
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

fun DependencyHandler.lombok(version: Any) {

  val dependencyNotation = "org.projectlombok:lombok:$version"

  implementation(dependencyNotation)
  testImplementation(dependencyNotation)

  annotationProcessor(dependencyNotation)
  testAnnotationProcessor(dependencyNotation)
}

tasks.withType<Wrapper> {
  gradleVersion = "8.2.1"
}
