// test/org/example/GitHubAPITest.groovy

package org.example

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*
import com.lesfurets.jenkins.unit.BasePipelineTest

class GitHubAPITest extends BasePipelineTest {
    def github
    def mockScript

    @Before
    void setUp() {
        super.setUp()
        mockScript = loadScript("vars/githubApi.groovy")
        github = new GitHubAPITestWrapper(mockScript)
    }

    @Test
    void testGetRepository() {
        def result = github.getRepository("owner", "repo")
        assertEquals("repo", result.name)
        assertEquals("owner", result.owner.login)
    }

    @Test
    void testCreatePullRequest() {
        def result = github.createPullRequest("owner", "repo", "New feature", "feature-branch", "main", "Please review")
        assertEquals(1, result.number)
        assertEquals("New feature", result.title)
    }

    @Test
    void testGetBranches() {
        def result = github.getBranches("owner", "repo")
        assertEquals(2, result.size())
        assertEquals("main", result[0].name)
        assertEquals("develop", result[1].name)
    }

    class GitHubAPITestWrapper extends GitHubAPI {
        GitHubAPITestWrapper(def steps) {
            super(steps, "https://api.github.com", "github-token")
        }

        @Override
        protected def apiCall(String method, String endpoint, def payload = null) {
            // Mock API responses
            switch (endpoint) {
                case ~/\/repos\/.*/:
                    return [name: "repo", owner: [login: "owner"]]
                case ~/\/repos\/.*\/pulls/:
                    return [number: 1, title: payload.title]
                case ~/\/repos\/.*\/branches/:
                    return [[name: "main"], [name: "develop"]]
                default:
                    return [:]
            }
        }

        @Override
        protected String getToken() {
            return "mock-token"
        }
    }
}
