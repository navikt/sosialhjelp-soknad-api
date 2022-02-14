package no.nav.sosialhjelp.soknad.innsending

object SenderUtils {
    fun createPrefixedBehandlingsId(behandlingsId: String?, env: String): String {
        return "$env-$behandlingsId"
    }
}
