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
                               url: 'CHANGE_GIT_URL',
                               credentialsId: 'CHANGE_GITLAB_CREDENTIALS',
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
                                    sh "mvn clean install"
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