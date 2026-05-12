def runCommand(String unixCommand, String windowsCommand = null) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand ?: unixCommand
    }
}

pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS_ID = 'dockerhub_credentials'
        DOCKER_IMAGE = 'your-dockerhub-username/teedy-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        BASE_IMAGE = 'public.ecr.aws/ubuntu/ubuntu:22.04'
        APT_MIRROR = 'http://mirrors.aliyun.com/ubuntu/'
    }

    stages {
        stage('Build') {
            steps {
                checkout scm
                script {
                    runCommand('mvn -B clean package -DskipTests', 'mvn -B clean package -DskipTests')
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    runCommand(
                        "docker build --build-arg BASE_IMAGE=${env.BASE_IMAGE} --build-arg APT_MIRROR=${env.APT_MIRROR} -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} -t ${env.DOCKER_IMAGE}:latest .",
                        "docker build --build-arg BASE_IMAGE=${env.BASE_IMAGE} --build-arg APT_MIRROR=${env.APT_MIRROR} -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} -t ${env.DOCKER_IMAGE}:latest ."
                    )
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${env.DOCKER_HUB_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_PASSWORD'
                )]) {
                    script {
                        runCommand(
                            'echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin',
                            'powershell -NoProfile -Command "$env:DOCKERHUB_PASSWORD | docker login -u $env:DOCKERHUB_USERNAME --password-stdin"'
                        )
                        runCommand(
                            "docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}",
                            "docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                        )
                        runCommand(
                            "docker push ${env.DOCKER_IMAGE}:latest",
                            "docker push ${env.DOCKER_IMAGE}:latest"
                        )
                    }
                }
            }
        }

        stage('Run Containers') {
            steps {
                script {
                    [8082, 8083, 8084].each { port ->
                        def name = "teedy-container-${port}"
                        runCommand(
                            "docker stop ${name} || true",
                            "docker stop ${name} || exit /b 0"
                        )
                        runCommand(
                            "docker rm ${name} || true",
                            "docker rm ${name} || exit /b 0"
                        )
                        runCommand(
                            "docker run --name ${name} -d -p ${port}:8080 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}",
                            "docker run --name ${name} -d -p ${port}:8080 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                        )
                    }
                    runCommand(
                        'docker ps --filter "name=teedy-container"',
                        'docker ps --filter "name=teedy-container"'
                    )
                }
            }
        }
    }

    post {
        always {
            script {
                runCommand('docker logout || true', 'docker logout || exit /b 0')
            }
            archiveArtifacts artifacts: '**/target/**/*.jar, **/target/**/*.war', fingerprint: true, allowEmptyArchive: true
        }
    }
}
