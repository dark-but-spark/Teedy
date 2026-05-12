def runCommand(String unixCommand, String windowsCommand = null) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand ?: unixCommand
    }
}

pipeline {
    agent any

    parameters {
        string(name: 'DOCKER_IMAGE', defaultValue: 'your-dockerhub-username/teedy-app', description: 'Docker Hub repository, for example: yourname/teedy-app')
        string(name: 'DOCKER_HUB_CREDENTIALS_ID', defaultValue: 'dockerhub_credentials', description: 'Jenkins username/password credentials ID for Docker Hub')
        string(name: 'BASE_IMAGE', defaultValue: 'public.ecr.aws/ubuntu/ubuntu:22.04', description: 'Base Ubuntu image used by Dockerfile')
        string(name: 'APT_MIRROR', defaultValue: 'http://mirrors.aliyun.com/ubuntu/', description: 'Ubuntu apt mirror used during docker build')
    }

    environment {
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Validate Config') {
            steps {
                script {
                    if (params.DOCKER_IMAGE == 'your-dockerhub-username/teedy-app') {
                        error 'Please set DOCKER_IMAGE to your real Docker Hub repository, for example: cjy/teedy-app'
                    }
                }
            }
        }

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
                        "docker build --build-arg BASE_IMAGE=${params.BASE_IMAGE} --build-arg APT_MIRROR=${params.APT_MIRROR} -t ${params.DOCKER_IMAGE}:${env.DOCKER_TAG} -t ${params.DOCKER_IMAGE}:latest .",
                        "docker build --build-arg BASE_IMAGE=${params.BASE_IMAGE} --build-arg APT_MIRROR=${params.APT_MIRROR} -t ${params.DOCKER_IMAGE}:${env.DOCKER_TAG} -t ${params.DOCKER_IMAGE}:latest ."
                    )
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${params.DOCKER_HUB_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_PASSWORD'
                )]) {
                    script {
                        runCommand(
                            'echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin',
                            'powershell -NoProfile -Command "$env:DOCKERHUB_PASSWORD | docker login -u $env:DOCKERHUB_USERNAME --password-stdin"'
                        )
                        runCommand(
                            "docker push ${params.DOCKER_IMAGE}:${env.DOCKER_TAG}",
                            "docker push ${params.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                        )
                        runCommand(
                            "docker push ${params.DOCKER_IMAGE}:latest",
                            "docker push ${params.DOCKER_IMAGE}:latest"
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
                            "docker run --name ${name} -d -p ${port}:8080 ${params.DOCKER_IMAGE}:${env.DOCKER_TAG}",
                            "docker run --name ${name} -d -p ${port}:8080 ${params.DOCKER_IMAGE}:${env.DOCKER_TAG}"
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
