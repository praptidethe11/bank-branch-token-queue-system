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
        APP_URL    = 'http://localhost:8082/'
        IMAGE_NAME = 'praptidethe/token-queue-system'
        IMAGE_TAG  = "1.0.${env.BUILD_NUMBER}"
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

        stage('Docker Build') {
            steps {
                archiveArtifacts artifacts: 'target/token-queue-system.war', fingerprint: true
                bat 'docker build --label git.commit=%GIT_COMMIT% --label jenkins.build=%BUILD_NUMBER% -t %IMAGE_NAME%:%IMAGE_TAG% -t %IMAGE_NAME%:latest .'
                bat 'docker images %IMAGE_NAME%'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    bat 'echo %DH_PASS%| docker login -u %DH_USER% --password-stdin'
                    bat 'docker push %IMAGE_NAME%:%IMAGE_TAG%'
                    bat 'docker push %IMAGE_NAME%:latest'
                    bat 'docker logout'
                }
            }
        }

        stage('Deploy Container') {
            steps {
                bat(returnStatus: true, script: 'docker rm -f token-queue')
                bat 'docker run -d --name token-queue -p 9090:8082 %IMAGE_NAME%:%IMAGE_TAG%'
                powershell '''
                    $ready = $false
                    for ($i = 0; $i -lt 30; $i++) {
                        try {
                            Invoke-WebRequest -Uri 'http://localhost:9090/' -UseBasicParsing -TimeoutSec 3 | Out-Null
                            $ready = $true
                            break
                        } catch {
                            Start-Sleep -Seconds 3
                        }
                    }
                    if (-not $ready) { throw 'Container did not respond on port 9090' }
                    Write-Output 'Container is up on port 9090'
                '''
                bat 'docker ps --filter name=token-queue'
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