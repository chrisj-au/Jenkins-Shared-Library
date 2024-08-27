// test/org/example/GitHubAPITest.groovy
package org.slib

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.mockito.Mockito
import java.net.HttpURLConnection
import java.net.URL

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockito.Mockito.*

class GitHubAPITest {

    private GitHubAPI api
    private def stepsMock

    @BeforeEach
    void setUp() {
        stepsMock = mock(Object.class)
        api = new GitHubAPI(stepsMock, 'https://api.github.com', 'fake-credentials-id')

        // Mock HttpURLConnection
        def connectionMock = mock(HttpURLConnection.class)
        def urlMock = mock(URL.class)

        when(urlMock.openConnection()).thenReturn(connectionMock)
        GroovySystem.metaClassRegistry.removeMetaClass(URL)
        URL.metaClass.constructor = { String urlString -> urlMock }

        when(connectionMock.getResponseCode()).thenReturn(200)
        when(connectionMock.getInputStream()).thenReturn(new ByteArrayInputStream('{"name": "test-repo"}'.bytes))
    }

    @AfterEach
    void tearDown() {
        // Clean up the metaClass manipulation after each test
        GroovySystem.metaClassRegistry.removeMetaClass(URL)
    }

    @Test
    void testGetRepository() {
        def repo = api.getRepository('owner', 'test-repo')
        assertEquals('test-repo', repo.name)
    }
}
