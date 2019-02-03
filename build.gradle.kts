plugins {
   `java-library`
}

group = "de.pieroavola"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    api("org.reflections:reflections:0.9.11")
    
    api("org.slf4j:slf4j-api:1.7.25")
    api("org.slf4j:slf4j-simple:1.7.25")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")

    testImplementation("org.assertj:assertj-core:3.11.1")
}

tasks.withType(Test::class) {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(Wrapper::class) {
    gradleVersion = "5.1.1"
}
