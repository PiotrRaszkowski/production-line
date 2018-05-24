pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK8'
    }
    
    options{
       gitlabBuilds(builds: ["Preparation","Build"])    
       gitLabConnection('gitlab')
    }
    
    stages {
        stage('Preparation') {
            steps {
                gitlabCommitStatus(name: 'Preparation') {
                    script {
                        try {
                            cleanWs()
                        
                            echo " Merge request: ${gitlabSourceBranch} => ${gitlabTargetBranch} "
                            git(
                               url: 'http://gitlab/root/spring-hello-world.git',
                               credentialsId: 'gitlab-https',
                               branch: '${gitlabTargetBranch}'
                            )
                            
                            sh "git merge --ff origin/${gitlabSourceBranch}"
            				sh "git reset --hard"
            				sh "git checkout -f origin/${gitlabSourceBranch}"
                        } catch(error) {
                            currentBuild.result = 'FAILURE' 
        				    throw error
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                gitlabCommitStatus(name: 'Build') {
                    timeout(30) {
                        ansiColor('xterm') {
                            script {
                                try {
                                    sh "mvn clean install findbugs:findbugs"
                                }catch(error){
                                    currentBuild.result = 'FAILURE' 
                                    throw error
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('FindBugs') {
            steps {
               findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '**/pl.raszkowski.example/**', pattern: '**/target/findbugsXml.xml', unHealthy: ''
            }
	   }
    }
    
    post {
		always {
			script {
			    echo "Post stage"
				junit '**/target/*-reports/*.xml'
				currentBuild.result = currentBuild.result ?: 'SUCCESS'
			}
		}
	}
}