#!/usr/bin/env groovy

package org.slib

def checkOut(
    String url,
    String credentialId,
    String branchName = 'master') {
    
    echo "Checking out repo ${url}"
    
    repoDetail = checkout scm: [
                    $class: 'GitSCM',
                    branches: [[name: "${branchName}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'LocalBranch', localBranch: "**"]],
                    submoduleCfg: [],
                    userRemoteConfigs: [[
                        credentialsId: credentialId, url: url]]
                    ]

    return repoDetail
}

def getDetails() {
    // Read Git details
    authorName = sh(returnStdout: true, script: "git log -n 1 --pretty=%an").trim()
    authorEmail = sh(returnStdout: true, script: "git log -n 1 --pretty=%ae").trim()
    def hash =  sh(returnStdout: true, script: "git log -n 1 --pretty='format:%H'").trim()

    // Echo Git details
    echo "Commit Hash: ${hash}"
    echo "Author: ${authorName} - ${authorEmail}"
    return authorEmail
}

def getTags() {
    echo "Checking if current commit has a tag"

    def commitTag  = sh (returnStdout: true, script: """ 
    git tag -l --points-at HEAD
    """).replaceAll("\\s","")

    if (commitTag?.trim()) {
        echo "Commit has the following tag: ${commitTag}"
        //env.BUILD_VERSION=commitTag.replaceAll("\\s","")
        def tags = commitTag.replaceAll("\\s","")
        return tags
    }
    else {
        echo "There is no tag on this commit. Using job number"
        return null
        // env.BUILD_VERSION=env.BUILD_NUMBER
    }
  }

def lastCommit(String sinceDays) {
    recentCommit = sh(returnStdout: true, script: "git log -n 1 --since=\"${sinceDays} days ago\" --format=%cd").trim()
    return recentCommit
}