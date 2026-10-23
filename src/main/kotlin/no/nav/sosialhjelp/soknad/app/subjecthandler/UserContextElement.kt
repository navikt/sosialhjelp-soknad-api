package no.nav.sosialhjelp.soknad.app.subjecthandler

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class UserContextElement(
    val userToken: String,
    val userId: String,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<UserContextElement>
}

suspend fun currentUserContext(): UserContextElement =
    currentCoroutineContext()[UserContextElement] ?: error("Fant ikke userContext")
