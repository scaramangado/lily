node {

    checkout scm

    stages {
        stage("Test") {
            steps {
                sh "gradlew test"
            }
        }

        stage("Build") {
            steps {
                sh "gradlew build -x test"
            }
        }
    }
}
