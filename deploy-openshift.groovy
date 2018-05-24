pipeline {
    environment {
        REGISTRY_URL = ''
        REGISTRY_PASSWORD = ''
      }
    
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
                       url: 'GITLAB_URL',
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
                                sh "docker login -p ${env.REGISTRY_PASSWORD} -u jenkins ${env.REGISTRY_URL}"
                                sh "mvn clean install -DskipTests -Ddocker.repository.address=${env.REGISTRY_URL} -Ddocker.repository.project=OPENSHIFT_PROJECT -Ddocker.skipPush=false -Pbuild-docker-image"
                            }catch(error){
                                junit '**/target/*-reports/*.xml'
                            }
                        }
                    }
                }
            }
        }
    }
}