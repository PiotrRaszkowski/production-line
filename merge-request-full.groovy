pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK8'
    }
    
    options{
       gitlabBuilds(builds: ["Preparation","Build", "Code coverage"])    
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
                                    sh "mvn clean install -Pcode-coverage findbugs:findbugs"
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
        
        stage('Code coverage') {
            steps {
                script {
                        jacoco( 
                            execPattern: '**/target/coverage-reports/*.exec',
                            //classPattern: 'target/classes',
                            //sourcePattern: 'src/main/java',
                            exclusionPattern: 'src/test*',
                            minimumBranchCoverage : '0', maximumBranchCoverage: '0',
                            minimumClassCoverage : '0', maximumClassCoverage: '0',
                            minimumComplexityCoverage : '0', maximumComplexityCoverage: '0',
                            minimumInstructionCoverage: '0', maximumInstructionCoverage: '0',
                            minimumLineCoverage : '10', maximumLineCoverage: '20',
                            minimumMethodCoverage : '0', maximumMethodCoverage: '0',
                            buildOverBuild: false,
                            changeBuildStatus: true,
                        )
                        
                        if(currentBuild.result == 'FAILURE' || currentBuild.result == 'UNSTABLE') {
                            updateGitlabCommitStatus name: 'Code coverage', state: 'failed'
                        } else{
                            updateGitlabCommitStatus name: 'Code coverage', state: 'success'
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