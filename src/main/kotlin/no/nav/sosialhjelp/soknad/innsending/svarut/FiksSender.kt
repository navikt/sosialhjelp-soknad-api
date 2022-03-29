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
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtService
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import java.sql.Date
import java.util.UUID
import java.util.stream.Collectors

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

    fun sendTilFiks(sendtSoknad: SendtSoknad): String? {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val forsendelse = createForsendelse(sendtSoknad, filnavnInputStreamMap)
        return svarUtService.send(forsendelse, filnavnInputStreamMap)
    }

    fun createForsendelse(sendtSoknad: SendtSoknad, map: HashMap<String, InputStream>): Forsendelse {
        val soknadUnderArbeid = innsendingService.hentSoknadUnderArbeid(sendtSoknad.behandlingsId, sendtSoknad.eier)
        val svarPaForsendelseId = getSvarPaForsendelseId(sendtSoknad, soknadUnderArbeid)
        val fakeAdresse = PostAdresse()
            .withNavn(sendtSoknad.navEnhetsnavn)
            .withPostNummer("0000")
            .withPostSted("Ikke send")
        validerAtEttersendelseSinSoknadHarForsendelseId(sendtSoknad, svarPaForsendelseId)
        return Forsendelse()
            .withMottaker(
                Adresse()
                    .withDigitalAdresse(Digitaladresse().withOrganisasjonsNummer(sendtSoknad.orgnummer))
                    .withPostAdresse(fakeAdresse)
            )
            .withAvgivendeSystem("digisos_avsender")
            .withForsendelsesType("nav.digisos")
            .withEksternReferanse(getBehandlingsId(sendtSoknad))
            .withTittel(if (sendtSoknad.erEttersendelse) ETTERSENDELSE_TIL_NAV else SOKNAD_TIL_NAV)
            .withKunDigitalLevering(false)
            .withUtskriftsKonfigurasjon(fakeUtskriftsConfig)
            .withKryptert(krypteringEnabled)
            .withKrevNiva4Innlogging(krypteringEnabled)
            .withSvarPaForsendelse(svarPaForsendelseId)
            .withDokumenter(hentDokumenterFraSoknad(soknadUnderArbeid, map))
            .withMetadataFraAvleverendeSystem(
                NoarkMetadataFraAvleverendeSaksSystem()
                    .withDokumentetsDato(Date.valueOf(sendtSoknad.brukerFerdigDato.toLocalDate()))
            )
    }

    private fun getBehandlingsId(sendtSoknad: SendtSoknad): String? {
        return if (MiljoUtils.isNonProduction()) {
            createPrefixedBehandlingsId(sendtSoknad.behandlingsId)
        } else {
            sendtSoknad.behandlingsId
        }
    }

    private fun getSvarPaForsendelseId(
        sendtSoknad: SendtSoknad,
        soknadUnderArbeid: SoknadUnderArbeid
    ): ForsendelsesId? {
        return if (sendtSoknad.erEttersendelse && innsendingService.finnSendtSoknadForEttersendelse(soknadUnderArbeid).fiksforsendelseId != null) {
            ForsendelsesId()
                .withId(UUID.fromString(innsendingService.finnSendtSoknadForEttersendelse(soknadUnderArbeid).fiksforsendelseId))
        } else null
    }

    private fun validerAtEttersendelseSinSoknadHarForsendelseId(
        sendtSoknad: SendtSoknad,
        svarPaForsendelseId: ForsendelsesId?
    ) {
        check(
            !(
                sendtSoknad.erEttersendelse && (
                    svarPaForsendelseId == null || svarPaForsendelseId.id == null || svarPaForsendelseId.id.toString()
                        .isEmpty()
                    )
                )
        ) {
            "Ettersendelse med behandlingsId " + sendtSoknad.behandlingsId +
                " er knyttet til en søknad med behandlingsId " + sendtSoknad.tilknyttetBehandlingsId +
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
        log.info(
            "Antall vedlegg: {}. Antall vedlegg lastet opp av bruker: {}",
            antallFiksDokumenter,
            antallVedleggForsendelse
        )
        try {
            val opplastedeVedleggstyper = internalSoknad.vedlegg.vedlegg.stream()
                .filter { jsonVedlegg: JsonVedlegg -> jsonVedlegg.status == "LastetOpp" }
                .collect(Collectors.toList())
            var antallBrukerOpplastedeVedlegg = 0
            for (vedlegg in opplastedeVedleggstyper) {
                antallBrukerOpplastedeVedlegg += vedlegg.filer.size
            }
            if (antallVedleggForsendelse != antallBrukerOpplastedeVedlegg) {
                log.warn(
                    "Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: {}, forsendelse til Fiks: {}. Er ettersendelse: {}",
                    antallBrukerOpplastedeVedlegg,
                    antallVedleggForsendelse,
                    soknadUnderArbeid.erEttersendelse
                )
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
