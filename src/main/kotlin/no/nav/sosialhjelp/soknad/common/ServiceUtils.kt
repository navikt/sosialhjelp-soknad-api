package no.nav.sosialhjelp.soknad.common

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
open class ServiceUtils(
    private val env: Environment
) {
    open fun isMockAltProfil(): Boolean {
        return env.activeProfiles.any { it.equals("mock-alt") || it.equals("test") }
    }
}
