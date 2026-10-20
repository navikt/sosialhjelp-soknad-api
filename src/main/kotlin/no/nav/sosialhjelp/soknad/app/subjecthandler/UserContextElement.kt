package no.nav.sosialhjelp.soknad.app.subjecthandler

import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class UserContextElement(
    val userToken: String,
    val userId: String,
) : ThreadContextElement<UserContextElement?>,
    AbstractCoroutineContextElement(Key) {
    override fun updateThreadContext(context: CoroutineContext): UserContextElement? =
        currentUserContext.get().also { currentUserContext.set(this) }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: UserContextElement?,
    ) {
        if (oldState == null) currentUserContext.remove() else currentUserContext.set(oldState)
    }

    companion object Key : CoroutineContext.Key<UserContextElement> {
        private val currentUserContext = ThreadLocal<UserContextElement?>()

        fun current(): UserContextElement? = currentUserContext.get()
    }
}

suspend fun currentUserContext(): UserContextElement =
    currentCoroutineContext()[UserContextElement] ?: error("Fant ikke userContext")
