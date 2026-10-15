pipeline {
    agent any
    // keep your existing tools { } block here

    environment {
        TOMCAT_WEBAPPS = 'PUT_YOUR_TOMCAT_webapps_PATH_HERE'
        STAGING_URL    = 'http://localhost:8081/token-queue-staging/'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { bat 'mvn clean package' }
        }
        stage('Deploy to Staging') {
            steps {
                bat 'copy /Y target\\token-queue-system.war "%TOMCAT_WEBAPPS%\\token-queue-staging.war"'
                sleep time: 25, unit: 'SECONDS'
            }
        }
        stage('Selenium UI Tests') {
            steps {
                bat 'mvn test -Pselenium -Dbase.url=%STAGING_URL% -Dheadless=true'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
                }
            }
        }
        stage('Deploy to Production') {
            steps {
                // paste your existing Task 8 deploy step here
            }
        }
    }
}