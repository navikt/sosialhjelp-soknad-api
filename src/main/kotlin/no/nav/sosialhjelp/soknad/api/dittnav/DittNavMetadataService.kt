package no.nav.sosialhjelp.soknad.api.dittnav

import no.nav.sosialhjelp.soknad.api.LenkeUtils.lenkeTilPabegyntSoknad
import no.nav.sosialhjelp.soknad.api.TimeUtils.toUtc
import no.nav.sosialhjelp.soknad.api.dittnav.dto.PabegyntSoknadDto
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DittNavMetadataService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    fun hentAktivePabegynteSoknader(fnr: String): List<PabegyntSoknadDto> {
        return hentPabegynteSoknader(fnr, true)
    }

    fun hentInaktivePabegynteSoknader(fnr: String): List<PabegyntSoknadDto> {
        return hentPabegynteSoknader(fnr, false)
    }

    private fun hentPabegynteSoknader(fnr: String, aktiv: Boolean): List<PabegyntSoknadDto> {
        val pabegynteSoknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr, !aktiv)
        return pabegynteSoknader.map {
            PabegyntSoknadDto(
                toUtc(it.opprettetDato, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                eventId(it.behandlingsId, aktiv),
                it.behandlingsId,
                SOKNAD_TITTEL,
                lenkeTilPabegyntSoknad(it.behandlingsId),
                SIKKERHETSNIVAA_3, // hvis ikke vil ikke innloggede nivå 3 brukere se noe på DittNav
                toUtc(it.sistEndretDato, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                aktiv
            )
        }
    }

    fun oppdaterLestDittNavForPabegyntSoknad(behandlingsId: String?, fnr: String?): Boolean {
        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
        if (soknadMetadata == null) {
            log.warn("Fant ingen soknadMetadata med behandlingsId={}", behandlingsId)
            return false
        }
        soknadMetadata.lestDittNav = true
        return try {
            soknadMetadataRepository.oppdaterLestDittNav(soknadMetadata, fnr)
            true
        } catch (e: Exception) {
            log.warn(
                "Noe feilet ved oppdatering av lestDittNav for soknadMetadata med behandlingsId={}",
                behandlingsId,
                e
            )
            false
        }
    }

    private fun eventId(behandlingsId: String, aktiv: Boolean): String {
        return behandlingsId + "_" + if (aktiv) "aktiv" else "inaktiv"
    }

    companion object {
        private val log = LoggerFactory.getLogger(DittNavMetadataService::class.java)
        private const val SOKNAD_TITTEL = "Søknad om økonomisk sosialhjelp"
        private const val SIKKERHETSNIVAA_3 = 3
        private const val SIKKERHETSNIVAA_4 = 4
    }
}
