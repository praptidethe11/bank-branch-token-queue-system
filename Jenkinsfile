pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    tools {
        jdk 'JDK21'
        maven 'Maven-3.9.16'
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'test'],
               description: 'Target environment. Sets the application name on Tomcat.')
        string(name: 'TOMCAT_HOME', defaultValue: 'C:\\DevTools\\apache-tomcat-11.0.25',
               description: 'Tomcat installation folder')
        string(name: 'TOMCAT_PORT', defaultValue: '8081',
               description: 'Tomcat HTTP port')
    }

    environment {
        WAR_NAME = "token-queue-system-${params.DEPLOY_ENV}"
        APP_URL  = "http://localhost:${params.TOMCAT_PORT}/token-queue-system-${params.DEPLOY_ENV}/"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package'
                archiveArtifacts artifacts: 'target/token-queue-system.war', fingerprint: true
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    if not exist "%TOMCAT_HOME%\\webapps" (
                        echo ERROR: Tomcat webapps folder not found at %TOMCAT_HOME%
                        exit /b 1
                    )
                    copy /Y "target\\token-queue-system.war" "%TOMCAT_HOME%\\webapps\\%WAR_NAME%.war"
                '''
            }
        }

        stage('Verify') {
            steps {
                bat 'curl.exe -f -s -S -o NUL --retry 12 --retry-delay 5 --retry-all-errors --retry-connrefused "%APP_URL%"'
                echo "Application is up at ${env.APP_URL}"
            }
        }
    }

    post {
        success {
            echo "Deployed to ${env.APP_URL}"
        }
        failure {
            echo 'Pipeline failed. Check the stage that turned red.'
        }
    }
}