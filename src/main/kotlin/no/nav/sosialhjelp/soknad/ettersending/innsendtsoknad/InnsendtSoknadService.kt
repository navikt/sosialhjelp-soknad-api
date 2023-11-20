package no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad

import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.EttersendelseUtils.soknadSendtForMindreEnn30DagerSiden
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.Vedleggstatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.function.Predicate

@Component
class InnsendtSoknadService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    private val ikkeKvittering = Predicate<VedleggMetadata> { SKJEMANUMMER_KVITTERING != it.skjema }
    private val lastetOpp = Predicate<VedleggMetadata> { it.status?.er(Vedleggstatus.LastetOpp) ?: false }
    private val ikkeLastetOpp = lastetOpp.negate()
    private val datoFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    private val tidFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun hentBehandlingskjede(behandlingsId: String): BehandlingsKjede {
        val originalSoknad = hentOriginalSoknad(behandlingsId)
        val ettersendelser = originalSoknad?.let { hentEttersendelser(it.behandlingsId) }
        return BehandlingsKjede(
            originalSoknad = originalSoknad?.let { konverter(it) },
            ettersendelser = ettersendelser?.map { konverter(it) }
        )
    }

    private fun hentOriginalSoknad(behandlingsId: String): SoknadMetadata? {
        var soknad = soknadMetadataRepository.hent(behandlingsId)
        if (soknad?.type == SoknadMetadataType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            soknad = soknadMetadataRepository.hent(soknad.tilknyttetBehandlingsId)
        }
        return soknad
    }

    private fun hentEttersendelser(behandlingsId: String): List<SoknadMetadata> {
        return soknadMetadataRepository.hentBehandlingskjede(behandlingsId)
            .filter { it.status == SoknadMetadataInnsendingStatus.FERDIG }
            .sortedWith(Comparator.comparing { it.innsendtDato })
    }

    private fun konverter(metadata: SoknadMetadata): InnsendtSoknad {
        return InnsendtSoknad(
            behandlingsId = metadata.behandlingsId,
            innsendtDato = metadata.innsendtDato?.format(datoFormatter),
            innsendtTidspunkt = metadata.innsendtDato?.format(tidFormatter),
            soknadsalderIMinutter = soknadsalderIMinutter(metadata.innsendtDato),
            innsendteVedlegg = metadata.vedlegg?.vedleggListe?.let { tilVedlegg(it, lastetOpp) },
            ikkeInnsendteVedlegg = metadata.innsendtDato?.toLocalDate()
                ?.let { if (soknadSendtForMindreEnn30DagerSiden(it)) tilVedlegg(metadata.vedlegg?.vedleggListe, ikkeLastetOpp) else null },
            navenhet = metadata.navEnhet,
            orgnummer = metadata.orgnr
        )
    }

    fun getInnsendingstidspunkt(behandlingsId: String): LocalDateTime? {
        val soknadMetadata = hentOriginalSoknad(behandlingsId)
        return soknadMetadata?.innsendtDato
    }

    private fun tilVedlegg(
        vedlegg: List<VedleggMetadata>?,
        status: Predicate<VedleggMetadata>
    ): List<Vedlegg> {
        val vedleggMedRiktigStatus = vedlegg
            ?.filter { ikkeKvittering.test(it) }
            ?.filter { status.test(it) }

        val unikeVedlegg: MutableMap<String, Vedlegg> = HashMap()
        vedleggMedRiktigStatus?.forEach {
            val sammensattnavn = it.skjema + "|" + it.tillegg
            if (!unikeVedlegg.containsKey(sammensattnavn)) {
                unikeVedlegg[sammensattnavn] = Vedlegg(
                    skjemaNummer = it.skjema,
                    skjemanummerTillegg = it.tillegg,
                    innsendingsvalg = it.status
                )
            }
        }
        return unikeVedlegg.values.toList()
    }

    companion object {
        const val SKJEMANUMMER_KVITTERING = "L7"

        fun soknadsalderIMinutter(tidspunktSendt: LocalDateTime?): Long {
            return tidspunktSendt?.until(LocalDateTime.now(), ChronoUnit.MINUTES) ?: -1
        }
    }
}
