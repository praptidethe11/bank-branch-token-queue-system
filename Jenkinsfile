pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 15, unit: 'MINUTES')
    }

    environment {
        APP_URL = 'http://localhost:8082/'
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

        stage('Deploy') {
            steps {
                echo 'All Selenium tests passed. Deploying Bank Branch Token Queue System...'
                archiveArtifacts artifacts: 'target/token-queue-system.war', fingerprint: true
                // Use your Task deployment step here, for example:
                // bat 'copy /Y target\\token-queue-system.war "C:\\path\\to\\tomcat\\webapps\\"'
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
        success { echo 'Pipeline succeeded: tests passed and deployment completed.' }
        failure { echo 'Pipeline failed: deployment was blocked.' }
    }
}