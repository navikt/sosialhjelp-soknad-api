package no.nav.sosialhjelp.soknad.app.subjecthandler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class UserContextTest {
    @Test
    fun `makes user context available to blocking code on an IO thread`() =
        runBlocking {
            withContext(Dispatchers.IO + UserContextElement("token", "user")) {
                assertEquals("token", UserContextElement.current()?.userToken)
                assertEquals("user", UserContextElement.current()?.userId)
            }
        }

    @Test
    fun `restores outer user context after nested context`() =
        runBlocking(UserContextElement("outer-token", "outer-user")) {
            assertEquals("outer-token", UserContextElement.current()?.userToken)

            withContext(UserContextElement("inner-token", "inner-user")) {
                assertEquals("inner-token", UserContextElement.current()?.userToken)
            }

            assertEquals("outer-token", UserContextElement.current()?.userToken)
        }

    @Test
    fun `clears user context when coroutine completes`() {
        runBlocking(UserContextElement("token", "user")) {
            assertEquals("token", UserContextElement.current()?.userToken)
        }

        assertNull(UserContextElement.current())
    }
}
