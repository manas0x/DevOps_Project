pipeline {
    agent any
    options { disableConcurrentBuilds() }
    stages {
        stage('Test') { steps { sh 'mvn -B clean test' } }
        stage('Package') { steps { sh 'mvn -B -DskipTests package' } }
        stage('Deploy and verify') {
            steps { sh 'JENKINS_NODE_COOKIE=dontKillMe bash scripts/deploy.sh' }
        }
    }
    post { always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'; archiveArtifacts allowEmptyArchive: true, artifacts: 'target/*.jar' } }
}
