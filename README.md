# Jenkins-Shared-Library
Jenkins Shared Library


GitHub API

```
// Jenkinsfile

@Library('github-api-library') _

pipeline {
    agent any
    
    stages {
        stage('Use GitHub API') {
            steps {
                script {
                    def github = githubApi(credentialsId: 'github-token-credential-id')
                    
                    // Get repository information
                    def repo = github.getRepository('owner', 'repo-name')
                    echo "Repository name: ${repo.name}"
                    
                    // Create a pull request
                    def pr = github.createPullRequest('owner', 'repo-name', 'New feature', 'feature-branch', 'main', 'Please review this new feature')
                    echo "Created PR #${pr.number}"
                    
                    // Get branches
                    def branches = github.getBranches('owner', 'repo-name')
                    echo "Number of branches: ${branches.size()}"
                }
            }
        }
    }
}
```
