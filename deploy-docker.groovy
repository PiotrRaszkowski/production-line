pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK8'
    }
    
    options{
       gitLabConnection('gitlab')
    }
    
    stages {
        stage('Preparation') {
            steps {
                script {
                    git(
                       url: 'GIT_URL',
                       credentialsId: 'GITLAB_CREDENTIALS',
                       branch: "${BRANCH_NAME}"
                    )
                }
            }
        }
        
        stage('Build') {
            steps {
                timeout(30) {
                    ansiColor('xterm') {
                        script {
                            try {
                                sh "mvn clean install -DskipTests -Pbuild-docker-image"
                            }catch(error){
                                junit '**/target/*-reports/*.xml'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                timeout(30) {
                    ansiColor('xterm') {
                        script {
                            sh 'docker --version'
                            sh 'cd docker && docker build -t IMAGE_TAG --build-arg JAR_FILE=YOUR_JAR_FILE .'
                            sh 'docker stop CONTAINER_NAME || true && docker rm CONTAINER_NAME || true'
                            sh 'docker run -p 8080:8080 --name CONTAINER_NAME -d -t IMAGE_TAG'
                        }
                    }
                }
            }
        }
    }
}