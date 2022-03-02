package no.nav.sosialhjelp.soknad.api.saksoversikt

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingsSoknad
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.Hoveddokument
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegyntSoknad
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.Part
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.Vedlegg
import no.nav.sosialhjelp.soknad.api.LenkeUtils.lagEttersendelseLenke
import no.nav.sosialhjelp.soknad.api.LenkeUtils.lenkeTilPabegyntSoknad
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService.Companion.ETTERSENDELSE_FRIST_DAGER
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.isVedleggskravAnnet
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Properties

class SaksoversiktMetadataService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val ettersendingService: EttersendingService,
    private val navMessageSource: NavMessageSource,
    private val clock: Clock
) {
    fun hentInnsendteSoknaderForFnr(fnr: String): List<InnsendtSoknad> {
        val bundle = bundle
        val soknader = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr)
        return soknader.map {
            InnsendtSoknad()
                .withAvsender(
                    Part()
                        .withType(Part.Type.BRUKER)
                        .withVisningsNavn(bundle.getProperty("saksoversikt.mottaker.deg"))
                )
                .withMottaker(
                    Part()
                        .withType(Part.Type.NAV)
                        .withVisningsNavn(bundle.getProperty("saksoversikt.mottaker.nav"))
                )
                .withBehandlingsId(it.behandlingsId)
                .withInnsendtDato(tilDate(it.innsendtDato))
                .withHoveddokument(
                    Hoveddokument()
                        .withTittel(
                            if (it.type == SoknadType.SEND_SOKNAD_KOMMUNAL) {
                                bundle.getProperty("saksoversikt.soknadsnavn")
                            } else {
                                bundle.getProperty("saksoversikt.soknadsnavn.ettersending")
                            }
                        )
                )
                .withVedlegg(tilInnsendteVedlegg(it.vedlegg, bundle))
                .withTema("KOM")
                .withTemanavn(bundle.getProperty("saksoversikt.temanavn"))
                .withLenke(lagEttersendelseLenke(it.behandlingsId))
        }
    }

    fun hentPabegynteSoknaderForBruker(fnr: String): List<PabegyntSoknad> {
        val soknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr)
        return soknader.map {
            PabegyntSoknad()
                .withBehandlingsId(it.behandlingsId)
                .withTittel("Søknad om økonomisk sosialhjelp")
                .withSisteEndring(tilDate(it.sistEndretDato))
                .withLenke(lenkeTilPabegyntSoknad(it.behandlingsId))
        }
    }

    fun hentSoknaderBrukerKanEttersendePa(fnr: String): List<EttersendingsSoknad> {
        val bundle = bundle
        val ettersendelseFrist = LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong())
        val datoFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
        val soknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(fnr, ettersendelseFrist)
        return soknader.map {
            EttersendingsSoknad()
                .withBehandlingsId(it.behandlingsId)
                .withTittel(bundle.getProperty("saksoversikt.soknadsnavn") + " (" + it.innsendtDato.format(datoFormatter) + ")")
                .withLenke(lagEttersendelseLenke(it.behandlingsId))
                .withVedlegg(finnManglendeVedlegg(it, bundle))
        }
    }

    private fun finnManglendeVedlegg(soknad: SoknadMetadata, bundle: Properties): List<Vedlegg> {
        val nyesteSoknad = ettersendingService.hentNyesteSoknadIKjede(soknad)
        return nyesteSoknad.vedlegg.vedleggListe
            .asSequence()
            .filter { it.status.er(Vedleggstatus.VedleggKreves) }
            .filter { !isVedleggskravAnnet(it) }
            .map { "vedlegg." + it.skjema + "." + it.tillegg + ".tittel" }
            .distinct()
            .map { bundle.getProperty(it) }
            .map { Vedlegg().withTittel(it) }
            .toList()
    }

    private fun tilInnsendteVedlegg(vedlegg: VedleggMetadataListe, bundle: Properties): List<Vedlegg> {
        return vedlegg.vedleggListe
            .asSequence()
            .filter { it.status.er(Vedleggstatus.LastetOpp) }
            .map { "vedlegg." + it.skjema + "." + it.tillegg + ".tittel" }
            .distinct()
            .map { bundle.getProperty(it) }
            .map { Vedlegg().withTittel(it) }
            .toList()
    }

    private fun tilDate(innsendtDato: LocalDateTime): Date {
        return Date.from(innsendtDato.atZone(ZoneId.systemDefault()).toInstant())
    }

    private val bundle: Properties get() = navMessageSource.getBundleFor("soknadsosialhjelp", Locale("nb", "NO"))
}
