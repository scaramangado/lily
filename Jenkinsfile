pipeline {

    agent any

    stages {
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
