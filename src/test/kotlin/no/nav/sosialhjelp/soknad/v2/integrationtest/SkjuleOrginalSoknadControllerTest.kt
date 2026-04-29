package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.metadata.SkjuleOrginalSoknadController.Companion.createdSafetyZoneStart
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class SkjuleOrginalSoknadControllerTest: AbstractIntegrationTest() {

    @Test
    fun `Ingen soknad funnet skal returnere true`() {
        doGet(
            uri = getUrl(UUID.randomUUID()),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isTrue }
    }

    @Test
    fun `Opprettet innenfor SafetyZone skal returnere true`() {
        metadataRepository.save(
            SoknadMetadata(
                soknadId = UUID.randomUUID(),
                personId = "12345612345",
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(
                    opprettet = createdSafetyZoneStart.plusMinutes(1),
                    sendtInn = createdSafetyZoneStart.plusMinutes(20)
                ),
                mottakerKommunenummer = "0301",
                digisosId = UUID.randomUUID(),
                soknadType = SoknadType.STANDARD
            )
        )
            .let { metadata ->
                doGet(
                    uri = getUrl(metadata.digisosId!!),
                    responseBodyClass = Boolean::class.java,
                )
            }
            .also { shouldHide -> assertThat(shouldHide).isTrue }
    }

    @Test
    fun `Opprettet utenfor SafetyZone skal returnere false`() {
        metadataRepository.save(
            SoknadMetadata(
                soknadId = UUID.randomUUID(),
                personId = "12345612345",
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(
                    opprettet = createdSafetyZoneStart.minusMinutes(1),
                    sendtInn = createdSafetyZoneStart.plusMinutes(20)
                ),
                mottakerKommunenummer = "0301",
                digisosId = UUID.randomUUID(),
                soknadType = SoknadType.STANDARD
            )
        )
            .let { metadata ->
                doGet(
                    uri = getUrl(metadata.digisosId!!),
                    responseBodyClass = Boolean::class.java,
                )
            }
            .also { shouldHide -> assertThat(shouldHide).isFalse }
    }

    companion object {
        private fun getUrl(digisosId: UUID): String = "/soknad/hide/$digisosId"
    }

}
