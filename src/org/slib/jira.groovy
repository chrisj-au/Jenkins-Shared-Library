#!/usr/bin/env groovy

package org.slib 

// Wrapper function for either creating a new issue, or adding comments
def LogResult (String jiraProject, String assignTo, String startedBy, String failedStage) {
    
    // Making an asumption here that the assignTo is an email address from Git that can be resolved to a users SAM
    // Assumption here that assignTo is a user/email.  What if this was started by a commit?
    
    assignTo = assignTo.tokenize('@')[0]
    
    if (currentBuild.previousBuild.result == 'SUCCESS' || currentBuild.previousBuild.buildVariables.jiraIssue == null) {
        def descr = "Triggered by: ${startedBy}\nFailed at stage: ${failedStage}"
        issCreated = NewIssue('Task', jiraProject, "Jenkins Build ${env.BUILD_ID} Failed", descr, assignTo)
        env.jiraIssue = issCreated.key
    } else {
        env.jiraIssue = currentBuild.previousBuild.buildVariables.jiraIssue
        NewComment(env.jiraIssue, "Build is still failing, [${env.BUILD_ID}|${env.BUILD_URL}]")
    }
    return env.jiraIssue
}

def NewComment(
    String key = '',
    String description) {

    echo 'Adding comments to JIRA item'
    def comment = [ body: description ]
    def response = jiraAddComment idOrKey: key, input: comment
    return response
}

def GetUser(String searchText) {
    def users = jiraUserSearch queryStr: searchText
    return users.data
}

def GetIssue(String key) {
    response = jiraGetIssue idOrKey: key
    echo response.data.toString()
    return response.data
}

def NewIssue(
    String type = 'Task',
    String key,
    String summary, 
    String description,
    String assignedTo = '') {

    echo "Creating JIRA ${type}"

    script {
        def newIssue = [fields: [ project: [key: key],
            summary: summary,
            description: description,
            assignee: [name: assignedTo],
            issuetype: [name: type]]]

        response = jiraNewIssue issue: newIssue
        echo response.successful.toString() // true / false
        echo response.data.toString() // Example [id:10001, key:FIR-2, self:http://<url>/rest/api/2/issue/10001]

        return response.data
    }
}