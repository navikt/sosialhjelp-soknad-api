package no.nav.sosialhjelp.soknad.innsending.svarut

import no.ks.fiks.svarut.klient.model.Adresse
import no.ks.fiks.svarut.klient.model.Digitaladresse
import no.ks.fiks.svarut.klient.model.Dokument
import no.ks.fiks.svarut.klient.model.Forsendelse
import no.ks.fiks.svarut.klient.model.ForsendelsesId
import no.ks.fiks.svarut.klient.model.NoarkMetadataFraAvleverendeSaksSystem
import no.ks.fiks.svarut.klient.model.PostAdresse
import no.ks.fiks.svarut.klient.model.UtskriftsKonfigurasjon
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtService
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import java.sql.Date
import java.util.UUID

@Component
class FiksSender(
    dokumentKrypterer: DokumentKrypterer,
    private val innsendingService: InnsendingService,
    sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    @Value("\${feature.fiks.kryptering.enabled}") private val krypteringEnabled: Boolean,
    private val svarUtService: SvarUtService
) {
    private val fakeUtskriftsConfig = UtskriftsKonfigurasjon()
        .withUtskriftMedFarger(true)
        .withTosidig(true)

    private val fiksDokumentHelper = FiksDokumentHelper(krypteringEnabled, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator)

    fun sendTilFiks(soknadMetadata: SoknadMetadata): String? {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val forsendelse = createForsendelse(soknadMetadata, filnavnInputStreamMap)
        return svarUtService.send(forsendelse, filnavnInputStreamMap)
    }

    fun createForsendelse(soknadMetadata: SoknadMetadata, map: HashMap<String, InputStream>): Forsendelse {
        val soknadUnderArbeid = innsendingService.hentSoknadUnderArbeid(soknadMetadata.behandlingsId, soknadMetadata.fnr)
        val svarPaForsendelseId = getSvarPaForsendelseId(soknadMetadata, soknadUnderArbeid)
        val fakeAdresse = PostAdresse()
            .withNavn(soknadMetadata.navEnhet)
            .withPostNummer("0000")
            .withPostSted("Ikke send")
        validerAtEttersendelseSinSoknadHarForsendelseId(soknadMetadata, svarPaForsendelseId)
        return Forsendelse()
            .withMottaker(
                Adresse()
                    .withDigitalAdresse(Digitaladresse().withOrganisasjonsNummer(soknadMetadata.orgnr))
                    .withPostAdresse(fakeAdresse)
            )
            .withAvgivendeSystem("digisos_avsender")
            .withForsendelsesType("nav.digisos")
            .withEksternReferanse(getBehandlingsId(soknadMetadata))
            .withTittel(if (soknadMetadata.erEttersendelse) ETTERSENDELSE_TIL_NAV else SOKNAD_TIL_NAV)
            .withKunDigitalLevering(false)
            .withUtskriftsKonfigurasjon(fakeUtskriftsConfig)
            .withKryptert(krypteringEnabled)
            .withKrevNiva4Innlogging(krypteringEnabled)
            .withSvarPaForsendelse(svarPaForsendelseId)
            .withDokumenter(hentDokumenterFraSoknad(soknadUnderArbeid, map))
            .withMetadataFraAvleverendeSystem(
                NoarkMetadataFraAvleverendeSaksSystem()
                    .withDokumentetsDato(Date.valueOf(soknadMetadata.sistEndretDato.toLocalDate()))
            )
    }

    private fun getBehandlingsId(soknadMetadata: SoknadMetadata): String {
        return if (MiljoUtils.isNonProduction()) {
            createPrefixedBehandlingsId(soknadMetadata.behandlingsId)
        } else {
            soknadMetadata.behandlingsId
        }
    }

    private fun getSvarPaForsendelseId(
        soknadMetadata: SoknadMetadata,
        soknadUnderArbeid: SoknadUnderArbeid
    ): ForsendelsesId? {
        return if (soknadMetadata.erEttersendelse && innsendingService.finnFiksForsendelseIdForEttersendelse(soknadUnderArbeid) != null) {
            ForsendelsesId()
                .withId(UUID.fromString(innsendingService.finnFiksForsendelseIdForEttersendelse(soknadUnderArbeid)))
        } else null
    }

    private fun validerAtEttersendelseSinSoknadHarForsendelseId(
        soknadMetadata: SoknadMetadata,
        svarPaForsendelseId: ForsendelsesId?
    ) {
        check(!(soknadMetadata.erEttersendelse && svarPaForsendelseId?.id?.toString().isNullOrEmpty())) {
            "Ettersendelse med behandlingsId " + soknadMetadata.behandlingsId +
                " er knyttet til en søknad med behandlingsId " + soknadMetadata.tilknyttetBehandlingsId +
                " som ikke har mottat fiksForsendelseId. Innsending til SvarUt vil feile nå og bli forsøkt på nytt senere."
        }
    }

    fun hentDokumenterFraSoknad(soknadUnderArbeid: SoknadUnderArbeid, map: HashMap<String, InputStream>): List<Dokument> {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        if (internalSoknad == null) {
            throw RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler")
        } else if (!soknadUnderArbeid.erEttersendelse && internalSoknad.soknad == null) {
            throw RuntimeException("Kan ikke sende søknad fordi søknaden mangler")
        } else if (soknadUnderArbeid.erEttersendelse && internalSoknad.vedlegg == null) {
            throw RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler")
        }
        val fiksDokumenter: MutableList<Dokument> = ArrayList()
        val antallVedleggForsendelse: Int
        if (soknadUnderArbeid.erEttersendelse) {
            fiksDokumenter.add(
                fiksDokumentHelper.lagDokumentForEttersendelsePdf(
                    internalSoknad,
                    soknadUnderArbeid.eier,
                    map
                )
            )
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad, map))
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf(map))
            val dokumenterForVedlegg = fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid, map)
            antallVedleggForsendelse = dokumenterForVedlegg.size
            fiksDokumenter.addAll(dokumenterForVedlegg)
        } else {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSoknadJson(internalSoknad, map))
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSaksbehandlerPdf(internalSoknad, map))
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad, map))
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForJuridiskPdf(internalSoknad, map))
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf(map))
            val dokumenterForVedlegg = fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid, map)
            antallVedleggForsendelse = dokumenterForVedlegg.size
            fiksDokumenter.addAll(dokumenterForVedlegg)
        }
        val antallFiksDokumenter = fiksDokumenter.size
        log.info("Antall vedlegg: $antallFiksDokumenter. Antall vedlegg lastet opp av bruker: $antallVedleggForsendelse")
        try {
            val opplastedeVedleggstyper = internalSoknad.vedlegg.vedlegg
                .filter { jsonVedlegg: JsonVedlegg -> jsonVedlegg.status == "LastetOpp" }
            var antallBrukerOpplastedeVedlegg = 0
            for (vedlegg in opplastedeVedleggstyper) {
                antallBrukerOpplastedeVedlegg += vedlegg.filer.size
            }
            if (antallVedleggForsendelse != antallBrukerOpplastedeVedlegg) {
                log.warn("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: $antallBrukerOpplastedeVedlegg, forsendelse til Fiks: $antallVedleggForsendelse. Er ettersendelse: ${soknadUnderArbeid.erEttersendelse}")
            }
        } catch (e: RuntimeException) {
            log.debug("Ignored exception")
        }
        return fiksDokumenter
    }

    companion object {
        const val SOKNAD_TIL_NAV = "Søknad til NAV"
        const val ETTERSENDELSE_TIL_NAV = "Ettersendelse til NAV"
        private val log = LoggerFactory.getLogger(FiksSender::class.java)
    }
}
