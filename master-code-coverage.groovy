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
                                sh "mvn clean install -Pcode-coverage"
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
    }
}