#!/usr/bin/env groovy

package org.slib 


// Send the result of a Jenkins build
def sendBuildResult(
    String slackChannel,
    String startedBy,
    String authorName,
    String gitTag,
    failed_stage = null,
    String jiraURL = null) {

  // Template slack message, used for success and failure
  def messageTemplate = """
    *[${currentBuild.result}] ${env.JOB_NAME}*\n
    >>> <${env.BUILD_URL}|Jenkins Build ${env.BUILD_ID}>\n
    Triggered by: ${startedBy}\n
    Commit Tag: ${gitTag}"""

  def slackColor
  def prevSuccess = false
  
  // Track whether previous build was successful
  if (currentBuild.previousBuild.result == 'SUCCESS') {
    prevSuccess = true
  }

  if (currentBuild.result != "OK" && failed_stage) { 
    slackColor = "#FFFE89"
    slackMessage = ":warning:" + messageTemplate + "\nFailed Stage: ${failed_stage}\n"
    
    if (env.jiraIssue && jiraURL) { // Apply jira ticket details if provided
      // Show whether this is a new or existing ticket being updated
      slackMessage += """
        See: ${prevSuccess ? 'New' : 'Existing' } <${jiraURL}/browse/${env.jiraIssue}| Issue ${env.jiraIssue}>"""
      // Only show assigned to if this is a new issue
      if (prevSuccess) { slackMessage += " assigned to ${authorName}" }
    }
  } else {
    slackMessage = ":the_horns:" + messageTemplate
    slackColor = "#00ff00"
  }

  slackSend color: slackColor, channel: slackChannel, message:  slackMessage 
}

def notifyBuild(String buildStatus = 'STARTED', String details) {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'
  wrap([$class: 'BuildUser']) {
    buildUser = "${BUILD_USER}"
  }

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} \n DETAILS: ${details} \n JOB Triggered by: ${buildUser} \n (${env.BUILD_URL}) "

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  // Send notifications
  slackSend (color: colorCode, message: summary)
}