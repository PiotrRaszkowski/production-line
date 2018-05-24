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
                       branch: 'develop'
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
                                sh "mvn clean install -Pcode-coverage findbugs:findbugs"
                            }catch(error){
                                junit '**/target/*-reports/*.xml'
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
        
        stage('Results') {
			steps {
			  findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '**/CHANGE_YOUR_PACKAGE/**', pattern: '**/target/findbugsXml.xml', unHealthy: ''
			}
	   }
    }
}