package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.dto.SendTilUrlFrontend
import no.nav.sosialhjelp.soknad.innsending.dto.SoknadMottakerFrontend
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

@Service
class SoknadStatisticsService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val digisosApiService: DigisosApiService,
) {
    fun createSoknadSentDto(
        behandlingsId: String,
        digisosId: String,
    ): SendTilUrlFrontend {
        val jsonInternalSoknad =
            soknadUnderArbeidRepository
                .hentSoknad(behandlingsId, SubjectHandlerUtils.getUserIdFromToken())
                .jsonInternalSoknad ?: error("Fant ikke jsonInternalSoknad for behandlingsId=$behandlingsId")

        return SendTilUrlFrontend(
            id = digisosId,
            sendtTil = SoknadMottakerFrontend.FIKS_DIGISOS_API,
            antallDokumenter = jsonInternalSoknad.vedlegg.vedlegg.flatMap { it.filer }.size,
            kortSoknad = jsonInternalSoknad.soknad.data.soknadstype == JsonData.Soknadstype.KORT,
            forrigeSoknadSendt = hentForrigeSoknadSendt(behandlingsId),
        )
    }

    private fun hentForrigeSoknadSendt(behandlingsId: String): LocalDateTime? {
        return digisosApiService.getTimestampSistSendtSoknad(behandlingsId)
            ?.let {
                TimestampConverter.convertInstantToLocalDateTime(Instant.ofEpochMilli(it))
            }
    }
}
