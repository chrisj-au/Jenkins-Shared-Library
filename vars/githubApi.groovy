// vars/githubApi.groovy

import org.slib.GitHubAPI

def call(Map config = [:]) {
    def baseUrl = config.baseUrl ?: 'https://api.github.com'
    def credentialsId = config.credentialsId ?: 'github-token'
    return new GitHubAPI(this, baseUrl, credentialsId)
}
