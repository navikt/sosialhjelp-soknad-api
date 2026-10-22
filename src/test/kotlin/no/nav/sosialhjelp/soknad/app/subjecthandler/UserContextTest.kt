package no.nav.sosialhjelp.soknad.app.subjecthandler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class UserContextTest {
    @Test
    fun `makes user context available to suspend code on an IO thread`() =
        runBlocking {
            withContext(Dispatchers.IO + UserContextElement("token", "user")) {
                assertEquals("token", currentUserContext().userToken)
                assertEquals("user", currentUserContext().userId)
            }
        }

    @Test
    fun `restores outer user context after nested context`() =
        runBlocking(UserContextElement("outer-token", "outer-user")) {
            assertEquals("outer-token", currentUserContext().userToken)

            withContext(UserContextElement("inner-token", "inner-user")) {
                assertEquals("inner-token", currentUserContext().userToken)
            }

            assertEquals("outer-token", currentUserContext().userToken)
        }

    @Test
    fun `does not leak user context into a new coroutine`() {
        runBlocking(UserContextElement("token", "user")) {
            assertEquals("token", currentUserContext().userToken)
        }

        runBlocking {
            assertNull(currentCoroutineContext()[UserContextElement])
        }
    }
}
