package no.nav.sosialhjelp.soknad.innsending

object SenderUtils {
    fun createPrefixedBehandlingsId(behandlingsId: String?, environmentName: String): String {
        return "$environmentName-$behandlingsId"
    }
}
