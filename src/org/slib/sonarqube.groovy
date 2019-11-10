#!/usr/bin/env groovy

// Library for Scanning a site with SonarQube

package org.slib

def RunSonarQube(
    String sonarURL,
    String projectKey,
    String projectDisplayName = "",
    String projectExclusions = "",
    String srcPath = '.',
    String projectVersion = null,
    String scContainerBranch = "master",
    String ecrRepo,
    String sonarImage = "sonar-scanner:prod",
    String dockerAuth,
    String dockerRegion) {

    echo "SonarQube Helper" 
    echo "path: ${srcPath}"
    sh "ls -lah"
    dir ('sc_runner') {
        // Log into ECR to pull down Sonar Docker Image
        docker.withRegistry("https://${ecrRepo}", "ecr:${dockerRegion}:${dockerAuth}") 
        {
            // Run Docker Container using the passed through variables
            docker.image("${ecrRepo}/${sonarImage}").withRun("-v \"${srcPath}\":/sonar/src -e SONAR_URL=\"${sonarURL}\" -e PROJECT_KEY=\"${projectKey}\" -e PROJECT_NAME=\"${projectDisplayName}\" -e PROJECT_VERSION=\"${projectVersion}\" -e PROJECT_EXCLUSIONS=\"${projectExclusions}\"") {     
                c -> sh "docker logs -f ${c.id}"
                def status = sh(script: "docker wait ${c.id}", returnStdout: true).trim()
                echo "STATUS: ${status}"
                if (status!="0") {
                    sh 'exit 1'
                }
            }
        }
    }
}