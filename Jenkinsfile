pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 25, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    tools {
        jdk 'JDK21'
        maven 'Maven-3.9.16'
    }

    environment {
        APP_URL       = 'http://localhost:8082/'
        CONTAINER_URL = 'http://localhost:9090/'
        IMAGE_NAME    = 'praptidethe/token-queue-system'
        IMAGE_TAG     = "1.0.${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean package'
                archiveArtifacts artifacts: 'target/token-queue-system.war', fingerprint: true
            }
        }

        stage('Start Application') {
            steps {
                powershell '''
                    $dir = (Get-Location).Path
                    $cmd = "cmd /c java -jar $dir\\target\\token-queue-system.war > $dir\\app.log 2>&1"
                    $r = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{ CommandLine = $cmd; CurrentDirectory = $dir }
                    $r.ProcessId | Out-File app.pid -Encoding ascii
                    $ready = $false
                    for ($i = 0; $i -lt 40; $i++) {
                        try {
                            Invoke-WebRequest -Uri $env:APP_URL -UseBasicParsing -TimeoutSec 3 | Out-Null
                            $ready = $true
                            break
                        } catch {
                            Start-Sleep -Seconds 3
                        }
                    }
                    if (-not $ready) { throw 'Application did not start in time' }
                    Write-Output 'Application is up'
                '''
            }
        }

        stage('Selenium Tests') {
            steps {
                bat 'mvn -B test -Pselenium -Dheadless=true -DbaseUrl=%APP_URL%'
            }
            post {
                always {
                    bat(returnStatus: true, script: 'mvn -B surefire-report:report-only')
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
                    publishHTML(target: [
                        reportDir: 'target/site',
                        reportFiles: 'surefire-report.html',
                        reportName: 'Selenium Surefire Report',
                        keepAll: true,
                        alwaysLinkToLastBuild: true,
                        allowMissing: true
                    ])
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Stop Test Application') {
            steps {
                powershell '''
                    if (Test-Path app.pid) {
                        $id = Get-Content app.pid
                        taskkill /PID $id /T /F | Out-Null
                        Remove-Item app.pid -ErrorAction SilentlyContinue
                    }
                    exit 0
                '''
            }
        }

        stage('Docker Build') {
            steps {
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
                bat 'docker ps --filter name=token-queue'
            }
        }

        stage('Verify') {
            steps {
                bat 'curl.exe -f -s -S -o NUL --retry 12 --retry-delay 5 --retry-all-errors --retry-connrefused "%CONTAINER_URL%"'
                echo "Container is up at ${env.CONTAINER_URL}"
            }
        }
    }

    post {
        always {
            powershell '''
                if (Test-Path app.pid) {
                    $id = Get-Content app.pid
                    taskkill /PID $id /T /F | Out-Null
                    Remove-Item app.pid -ErrorAction SilentlyContinue
                }
                exit 0
            '''
            archiveArtifacts artifacts: 'app.log', allowEmptyArchive: true
        }
        success {
            echo "Pipeline succeeded: tests passed, image ${env.IMAGE_NAME}:${env.IMAGE_TAG} pushed and deployed at ${env.CONTAINER_URL}"
        }
        failure {
            echo 'Pipeline failed: image was not deployed or a stage failed.'
        }
    }
}