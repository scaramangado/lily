pipeline {

    agent any

    stages {

        stage("Info") {
            steps {
                sh "java -version"
                sh "./gradlew --version"
            }
        }

        stage("Test") {
            steps {
                sh "./gradlew test --info"
            }
        }

        stage("Build") {
            steps {
                sh "./gradlew build -x test"
            }
        }
    }
}
