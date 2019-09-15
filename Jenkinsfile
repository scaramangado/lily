pipeline {

    agent any

    stages {

        stage("Prepare") {
            steps {
                sh "./gradlew clean"
            }
        }

        stage("Test") {
            steps {
                sh "./gradlew test"
            }

            step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/test/*.xml' ])
        }

        stage("SonarQube") {
            steps {
                sh "./gradlew sonarqube"
            }
        }

        stage("Build") {
            steps {
                sh "./gradlew build -x test"
            }
        }
    }
}
