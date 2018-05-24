pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK8'
    }
    
    options{
       //gitlabBuilds(builds: ["Preparation","Build", "Results"])    
       gitLabConnection('gitlab')
    }
    
    stages {
        stage('Preparation') {
            steps {
                script {
                    git(
                       url: 'http://gitlab/root/spring-hello-world.git',
                       credentialsId: 'gitlab-https',
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
			  findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '**/pl.raszkowski.example/**', pattern: '**/target/findbugsXml.xml', unHealthy: ''
			}
	   }
    }
}