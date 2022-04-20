package no.nav.sosialhjelp.soknad.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
open class ServiceUtils(
    @Value("\${digisosapi.sending.enable}") private val digisosSendingEnabled: Boolean,
//    @Value("\${digisosapi.sending.alltidTilTestkommune.enable}") private val alltidSendTilTestkommune: Boolean,
//    @Value("\${digisosapi.henting.alltidTestkommune.enable}") private val alltidHentTestkommune: Boolean,
    private val env: Environment
) {

    open fun isSendingTilFiksEnabled(): Boolean {
        return digisosSendingEnabled
    }

    open fun isMockAltProfil(): Boolean {
        return env.activeProfiles.any { it.equals("mock-alt") || it.equals("test") }
    }

//    open fun isAlltidSendTilNavTestkommune(): Boolean {
//        return alltidSendTilTestkommune
//    }
//
//    open fun isAlltidHentKommuneInfoFraNavTestkommune(): Boolean {
//        return alltidHentTestkommune
//    }
}
