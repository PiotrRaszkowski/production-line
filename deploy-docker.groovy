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
                       url: 'CHANGE_GIT_URL',
                       credentialsId: 'CHANGE_GITLAB_CREDENTIALS',
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
                            sh 'cd docker && docker build -t CHANGE_IMAGE_TAG --build-arg JAR_FILE=CHANGE_YOUR_JAR_FILE .'
                            sh 'docker stop CHANGE_CONTAINER_NAME || true && docker rm CHANGE_CONTAINER_NAME || true'
                            sh 'docker run -p 8080:8080 --name CHANGE_CONTAINER_NAME -d -t CHANGE_IMAGE_TAG'
                        }
                    }
                }
            }
        }
    }
}