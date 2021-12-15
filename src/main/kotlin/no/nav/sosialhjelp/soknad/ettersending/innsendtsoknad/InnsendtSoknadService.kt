package no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad

import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.EttersendelseUtils.soknadSendtForMindreEnn30DagerSiden
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.function.Predicate

class InnsendtSoknadService(private val henvendelseService: HenvendelseService) {
    private val ikkeKvittering = Predicate<VedleggMetadata> { SKJEMANUMMER_KVITTERING != it.skjema }
    private val lastetOpp = Predicate<VedleggMetadata> { it.status.er(Vedleggstatus.LastetOpp) }
    private val ikkeLastetOpp = lastetOpp.negate()
    private val datoFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    private val tidFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun hentBehandlingskjede(behandlingsId: String): BehandlingsKjede {
        val originalSoknad = hentOriginalSoknad(behandlingsId)
        val ettersendelser = hentEttersendelser(originalSoknad.behandlingsId)
        return BehandlingsKjede(
            originalSoknad = konverter(originalSoknad),
            ettersendelser = ettersendelser.map { konverter(it) }
        )
    }

    private fun hentOriginalSoknad(behandlingsId: String): SoknadMetadata {
        var soknad = henvendelseService.hentSoknad(behandlingsId)
        if (soknad.type == SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            soknad = henvendelseService.hentSoknad(soknad.tilknyttetBehandlingsId)
        }
        return soknad
    }

    private fun hentEttersendelser(behandlingsId: String): List<SoknadMetadata> {
        return henvendelseService.hentBehandlingskjede(behandlingsId)
            .filter { it.status == SoknadMetadataInnsendingStatus.FERDIG }
            .sortedWith(Comparator.comparing { it.innsendtDato })
    }

    private fun konverter(metadata: SoknadMetadata): InnsendtSoknad {
        return InnsendtSoknad(
            behandlingsId = metadata.behandlingsId,
            innsendtDato = metadata.innsendtDato.format(datoFormatter),
            innsendtTidspunkt = metadata.innsendtDato.format(tidFormatter),
            soknadsalderIMinutter = soknadsalderIMinutter(metadata.innsendtDato),
            innsendteVedlegg = tilVedlegg(metadata.vedlegg.vedleggListe, lastetOpp),
            ikkeInnsendteVedlegg = if (soknadSendtForMindreEnn30DagerSiden(metadata.innsendtDato.toLocalDate())) tilVedlegg(metadata.vedlegg.vedleggListe, ikkeLastetOpp) else null,
            navenhet = metadata.navEnhet,
            orgnummer = metadata.orgnr
        )
    }

    fun getInnsendingstidspunkt(behandlingsId: String): LocalDateTime {
        val soknadMetadata = hentOriginalSoknad(behandlingsId)
        return soknadMetadata.innsendtDato
    }

    private fun tilVedlegg(
        vedlegg: List<VedleggMetadata>,
        status: Predicate<VedleggMetadata>
    ): List<Vedlegg> {
        val vedleggMedRiktigStatus = vedlegg
            .filter { ikkeKvittering.test(it) }
            .filter { status.test(it) }

        val unikeVedlegg: MutableMap<String, Vedlegg> = HashMap()
        vedleggMedRiktigStatus.forEach {
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