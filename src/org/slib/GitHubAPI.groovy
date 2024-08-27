// src/org/example/GitHubAPI.groovy

package org.example

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.net.HttpURLConnection
import java.net.URL

class GitHubAPI implements Serializable {
    private final String baseUrl
    private final String token
    private final def steps

    GitHubAPI(def steps, String credentialsId, String baseUrl = 'https://api.github.com') {
        this.steps = steps
        this.baseUrl = baseUrl
        this.token = fetchToken(credentialsId)
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

    private def apiCall(String method, String endpoint, Map payload = null) {
        HttpURLConnection connection = null
        try {
            def url = new URL("${baseUrl}${endpoint}")
            connection = (HttpURLConnection) url.openConnection()
            connection.setRequestMethod(method)
            connection.setRequestProperty("Authorization", "token ${token}")
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "Jenkins-GitHubAPI-Client")
            
            if (payload) {
                connection.setDoOutput(true)
                connection.setRequestProperty("Content-Type", "application/json")
                def outputStream = connection.getOutputStream()
                outputStream.write(JsonOutput.toJson(payload).getBytes("UTF-8"))
                outputStream.close()
            }

            int responseCode = connection.getResponseCode()
            def responseStream = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream()
            def responseText = responseStream.getText("UTF-8")
            def jsonResponse = new JsonSlurper().parseText(responseText)

            if (responseCode >= 200 && responseCode < 300) {
                return jsonResponse
            } else {
                steps.error("GitHub API Error: ${jsonResponse.message ?: 'Unknown error'} (HTTP ${responseCode})")
            }
        } catch (Exception e) {
            steps.error("Exception during GitHub API call: ${e.message}")
        } finally {
            if (connection != null) {
                connection.disconnect()
            }
        }
    }

    private String fetchToken(String credentialsId) {
        def token = null
        steps.withCredentials([steps.string(credentialsId: credentialsId, variable: 'GITHUB_TOKEN')]) {
            token = steps.env.GITHUB_TOKEN
        }
        if (!token) {
            steps.error("Failed to retrieve GitHub token with credentialsId: ${credentialsId}")
        }
        return token
    }
}
