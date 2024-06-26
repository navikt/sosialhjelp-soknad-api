package no.nav.sosialhjelp.soknad.v2.shadow

import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

fun TransactionTemplate.runWithNestedTransaction(function: () -> Unit): Result<Unit> {
    return kotlin.runCatching {
        propagationBehavior = TransactionDefinition.PROPAGATION_NESTED
        execute { function.invoke() }
    }
}
