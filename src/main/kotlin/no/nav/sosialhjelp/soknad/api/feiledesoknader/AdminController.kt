package no.nav.sosialhjelp.soknad.api.feiledesoknader

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.findOldSoknaderStatusSendt
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@ProtectedWithClaims("entraid")
@RequestMapping("/admin", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdminController(
    private val soknadMetadataService: SoknadMetadataService,
) {
    @GetMapping("/unresolved")
    fun hentIkkeMottattSoknader(): List<IkkeMottattSoknadDto> =
        soknadMetadataService
            .findOldSoknaderStatusSendt()
            .plus(soknadMetadataService.hentManueltKvittertUtSisteDag())
            .map { it.toDto() }

    @PostMapping("/soknad/{soknadId}/resolve")
    fun resolveApplication(
        @PathVariable soknadId: UUID,
    ) {
        soknadMetadataService.updateSoknadStatus(soknadId, SoknadStatus.MANUELT_KVITTERT_UT)
    }
}

private fun SoknadMetadataService.hentManueltKvittertUtSisteDag(): List<SoknadMetadata> =
    findMetadataForStatus(SoknadStatus.MANUELT_KVITTERT_UT)
        .filter { it.tidspunkt.sistEndret.isAfter(nowWithMillis().minusDays(1)) }


private fun SoknadMetadata.toDto(): IkkeMottattSoknadDto {
    requireNotNull(this.tidspunkt.sendtInn)
    requireNotNull(this.digisosId)
    requireNotNull(this.mottakerKommunenummer)
    return IkkeMottattSoknadDto(this.tidspunkt.sendtInn, this.soknadId, this.digisosId, this.mottakerKommunenummer, this.soknadType, this.status)
}

data class IkkeMottattSoknadDto(
    val innsendtTidspunkt: LocalDateTime,
    val soknadId: UUID,
    val digisosId: UUID,
    val kommunenummer: String,
    val soknadType: SoknadType,
    val status: SoknadStatus,
)
