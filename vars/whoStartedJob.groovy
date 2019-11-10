// Return who started a job.
//
// If it was started by a user manually, return the user name.
// If it was started by a github webhook, return the user that submitted the code.
// If it was started by a timer, return timer.
// If the cause can't be determined, return UNKNOWN
@NonCPS
def call() {
    def startedBy = "UNKNOWN"
    try {
        def buildCauses = currentBuild.rawBuild.getCauses()
        for ( buildCause in buildCauses ) {
            if (buildCause != null) {
                // echo "Build causes ${currentBuild.buildCauses}"
                // echo buildCause.getShortDescription()
                def group = ("${buildCause.getShortDescription()}" =~ /Started by (.*)/)
                startedBy = "${group[0][1]}"
                if (startedBy.contains("user")) {
                    return buildCause.getUserId() // Return login Id (upn possibly)
                    //return startedBy.tokenize()[1..2].join(" ") // Will return full name of person (so will userName??)
                }
                if (startedBy.contains("GitHub push")) {
                    return startedBy.tokenize()[3]
                }
                if (startedBy.contains("timer")) {
                    return startedBy
                }
            }
        }
    } catch(theError) {
        echo "Error getting user who started the build $theError"
    }

    return startedBy
}