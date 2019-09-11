plugins {
  `java-library`
  `maven-publish`
}

group = "de.scaramanga"
version = "0.1.0"

repositories {
  mavenCentral()
  jcenter()
}

dependencies {

  api("org.springframework.boot:spring-boot-starter:2.1.2.RELEASE")
  api("net.dv8tion:JDA:4.0.0_45") {
    exclude(module = "opus-java")
  }

  implementation("org.reflections:reflections:0.9.11")

  lombok("1.18.4")

  val jUnitVersion = "5.4.2"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.2.RELEASE") {
    exclude(module = "junit")
    exclude(module = "hamcrest-library")
    exclude(module = "hamcrest-core")
    exclude(module = "json-path")
    exclude(module = "jsonassert")
    exclude(module = "xmlunit-core")
  }

  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.awaitility:awaitility:3.1.6")
}

tasks.withType(Test::class) {
  useJUnitPlatform()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
  publications {
    create<MavenPublication>("Lily") {

      artifactId = "lily"
      from(components["java"])
    }
  }
}

tasks.withType(Wrapper::class) {
  gradleVersion = "5.6"
}

fun DependencyHandler.lombok(version: Any) {

  val dependencyNotation = "org.projectlombok:lombok:$version"

  implementation(dependencyNotation)
  testImplementation(dependencyNotation)

  annotationProcessor(dependencyNotation)
  testAnnotationProcessor(dependencyNotation)
}
