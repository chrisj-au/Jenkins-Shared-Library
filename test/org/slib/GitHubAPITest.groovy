// test/org/slib/GitHubAPITest.groovy
package org.slib

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockito.Mockito.*

class GitHubAPITest {

    private GitHubAPI api
    private def stepsMock

    @BeforeEach
    void setUp() {
        stepsMock = mock(Object.class)
        api = new GitHubAPI(stepsMock, 'https://api.github.com', 'fake-credentials-id')

        // Mock withCredentials
        when(stepsMock.withCredentials(any())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                def credentials = invocation.getArguments()[0]
                def token = 'mocked-token'
                System.setProperty('GITHUB_TOKEN', token)
                return null
            }
        })

        // Mock HttpURLConnection
        def connectionMock = mock(HttpURLConnection.class)
        def urlMock = mock(URL.class)

        when(urlMock.openConnection()).thenReturn(connectionMock)
        GroovySystem.metaClassRegistry.removeMetaClass(URL)
        URL.metaClass.constructor = { String urlString -> urlMock }

        when(connectionMock.responseCode).thenReturn(200)
        when(connectionMock.inputStream).thenReturn(new ByteArrayInputStream('{"name": "test-repo"}'.bytes))
    }

    @AfterEach
    void tearDown() {
        // Clean up the metaClass manipulation after each test
        GroovySystem.metaClassRegistry.removeMetaClass(URL)
        System.clearProperty('GITHUB_TOKEN')
    }

    @Test
    void testGetRepository() {
        def repo = api.getRepository('owner', 'test-repo')
        assertEquals('test-repo', repo.name)
    }
}
