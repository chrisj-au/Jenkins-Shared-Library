// src/org/example/GitHubAPI.groovy

package org.example

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class GitHubAPI implements Serializable {
    private String baseUrl
    private String credentialsId
    private def steps

    GitHubAPI(def steps, String baseUrl = 'https://api.github.com', String credentialsId) {
        this.steps = steps
        this.baseUrl = baseUrl
        this.credentialsId = credentialsId
    }

    def getRepository(String owner, String repo) {
        return apiCall("GET", "/repos/${owner}/${repo}")
    }

    def createPullRequest(String owner, String repo, String title, String head, String base, String body = '') {
        def payload = [
            title: title,
            head: head,
            base: base,
            body: body
        ]
        return apiCall("POST", "/repos/${owner}/${repo}/pulls", payload)
    }

    def getBranches(String owner, String repo) {
        return apiCall("GET", "/repos/${owner}/${repo}/branches")
    }

    protected def apiCall(String method, String endpoint, def payload = null) {
        def url = "${baseUrl}${endpoint}"
        def token = getToken()

        def curlCommand = ["curl", "-s", "-X", method, "-H", "Authorization: token ${token}", "-H", "Accept: application/vnd.github.v3+json"]

        if (payload) {
            def jsonPayload = JsonOutput.toJson(payload)
            curlCommand += ["-H", "Content-Type: application/json", "-d", jsonPayload]
        }

        curlCommand += [url]

        def response = executeCommand(curlCommand.join(' '))
        def result = new JsonSlurper().parseText(response)

        if (result.message && result.documentation_url) {
            steps.error("GitHub API error: ${result.message}")
        }

        return result
    }

    protected String getToken() {
        return steps.withCredentials([steps.string(credentialsId: credentialsId, variable: 'GITHUB_TOKEN')]) {
            return steps.env.GITHUB_TOKEN
        }
    }

    protected String executeCommand(String command) {
        return steps.sh(script: command, returnStdout: true).trim()
    }
}
