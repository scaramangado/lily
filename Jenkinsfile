pipeline {

    agent any

    stages {
        stage("Test") {
            steps {
                sh "./gradlew test --stacktrace"
            }
        }

        stage("Build") {
            steps {
                sh "./gradlew build -x test"
            }
        }
    }
}
