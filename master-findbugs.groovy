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
                       branch: 'master'
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
                                sh "mvn clean install findbugs:findbugs"
                            }catch(error){
                                junit '**/target/*-reports/*.xml'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Results') {
			steps {
			  findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '**/CHANGE_YOUR_PACKAGE/**', pattern: '**/target/findbugsXml.xml', unHealthy: ''
			}
	   }
    }
}