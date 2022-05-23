package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.metrics.Event
import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.getVedleggFromInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.navKontorTilInfluxNavn
import no.nav.sosialhjelp.soknad.metrics.SoknadMetricsService
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggRessurs.Companion.KS_MELLOMLAGRING_ENABLED
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DigisosApiService(
    private val digisosApiV1Client: DigisosApiV1Client,
    private val digisosApiV2Client: DigisosApiV2Client,
    private val henvendelseService: HenvendelseService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadMetricsService: SoknadMetricsService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val dokumentListeService: DokumentListeService,
    private val unleash: Unleash,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    private val digisosApiV2Enabled get() = unleash.isEnabled(KS_MELLOMLAGRING_ENABLED, false)

    fun sendSoknad(soknadUnderArbeid: SoknadUnderArbeid, token: String?, kommunenummer: String): String {
        return if (digisosApiV2Enabled) {
            sendSoknadMedDigisosApiV2(soknadUnderArbeid, token, kommunenummer)
        } else {
            sendSoknadMedDigisosApiV1(soknadUnderArbeid, token, kommunenummer)
        }
    }

    fun sendSoknadMedDigisosApiV1(soknadUnderArbeid: SoknadUnderArbeid, token: String?, kommunenummer: String): String {
        var behandlingsId = soknadUnderArbeid.behandlingsId
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke sende søknad hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid)
        log.info("Starter innsending av søknad med behandlingsId {}, skal sendes til DigisosApi v1", behandlingsId)
        val vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid)
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid, true)
        val filOpplastinger = dokumentListeService.lagDokumentListe(soknadUnderArbeid)
        log.info("Laster opp {}", filOpplastinger.size)
        val soknadJson = getSoknadJson(soknadUnderArbeid)
        val tilleggsinformasjonJson = getTilleggsinformasjonJson(jsonInternalSoknad.soknad)
        val vedleggJson = getVedleggJson(soknadUnderArbeid)

        if (MiljoUtils.isNonProduction()) {
            behandlingsId = createPrefixedBehandlingsId(behandlingsId)
        }
        val enhetsnummer = jsonInternalSoknad.soknad.mottaker.enhetsnummer
        val navEnhetsnavn = jsonInternalSoknad.soknad.mottaker.navEnhetsnavn
        log.info("Starter kryptering av filer for $behandlingsId, skal sende til kommune $kommunenummer med enhetsnummer $enhetsnummer og navenhetsnavn $navEnhetsnavn")
        val digisosId = sendOgKrypter(
            soknadJson,
            tilleggsinformasjonJson,
            vedleggJson,
            filOpplastinger,
            kommunenummer,
            navEnhetsnavn,
            behandlingsId,
            token
        )

        slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid)

        soknadMetricsService.reportSendSoknadMetrics(soknadUnderArbeid, vedlegg.vedleggListe)
        return digisosId
    }

    fun sendSoknadMedDigisosApiV2(soknadUnderArbeid: SoknadUnderArbeid, token: String?, kommunenummer: String): String {
        var behandlingsId = soknadUnderArbeid.behandlingsId
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke sende søknad hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid)
        log.info("Starter innsending av søknad med behandlingsId {}, skal sendes til DigisosApi v2", behandlingsId)
        val vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid)
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid, true)
        val filOpplastinger = dokumentListeService.lagDokumentListeForV2(soknadUnderArbeid)
        log.info("Laster opp {}", filOpplastinger.size)
        val soknadJson = getSoknadJson(soknadUnderArbeid)
        val tilleggsinformasjonJson = getTilleggsinformasjonJson(jsonInternalSoknad.soknad)
        val vedleggJson = getVedleggJson(soknadUnderArbeid)

        if (MiljoUtils.isNonProduction()) {
            behandlingsId = createPrefixedBehandlingsId(behandlingsId)
        }
        val enhetsnummer = jsonInternalSoknad.soknad.mottaker.enhetsnummer
        val navEnhetsnavn = jsonInternalSoknad.soknad.mottaker.navEnhetsnavn

        log.info("Starter kryptering av filer for $behandlingsId, skal sende til kommune $kommunenummer med enhetsnummer $enhetsnummer og navenhetsnavn $navEnhetsnavn")
        val digisosId = sendOgKrypter(
            soknadJson,
            tilleggsinformasjonJson,
            vedleggJson,
            filOpplastinger,
            kommunenummer,
            navEnhetsnavn,
            behandlingsId,
            token
        )

        slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid)

        soknadMetricsService.reportSendSoknadMetrics(soknadUnderArbeid, vedlegg.vedleggListe)
        return digisosId
    }

    private fun sendOgKrypter(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filOpplastinger: List<FilOpplasting>,
        kommunenr: String,
        navEnhetsnavn: String,
        behandlingsId: String,
        token: String?
    ): String {
        val event = lagForsoktSendtDigisosApiEvent(navEnhetsnavn)

        return try {
            if (digisosApiV2Enabled) {
                digisosApiV2Client.krypterOgLastOppFiler(
                    soknadJson = soknadJson,
                    tilleggsinformasjonJson = tilleggsinformasjonJson,
                    vedleggJson = vedleggJson,
                    dokumenter = filOpplastinger,
                    kommunenr = kommunenr,
                    navEksternRefId = behandlingsId,
                    token = token
                )
            } else {
                digisosApiV1Client.krypterOgLastOppFiler(
                    soknadJson = soknadJson,
                    tilleggsinformasjonJson = tilleggsinformasjonJson,
                    vedleggJson = vedleggJson,
                    dokumenter = filOpplastinger,
                    kommunenr = kommunenr,
                    navEksternRefId = behandlingsId,
                    token = token
                )
            }
        } catch (e: Exception) {
            event.setFailed()
            throw e
        } finally {
            event.report()
        }
    }

    private fun lagForsoktSendtDigisosApiEvent(navEnhetsnavn: String): Event {
        val event = MetricsFactory.createEvent("fiks.digisosapi.sendt")
        event.addTagToReport("mottaker", navKontorTilInfluxNavn(navEnhetsnavn))
        return event
    }

    fun getSoknadJson(soknadUnderArbeid: SoknadUnderArbeid): String {
        return try {
            val soknadJson = objectMapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad?.soknad)
            ensureValidSoknad(soknadJson)
            soknadJson
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere sonadJson", e)
        }
    }

    fun getTilleggsinformasjonJson(soknad: JsonSoknad?): String {
        if (soknad == null || soknad.mottaker == null) {
            log.error("Soknad eller soknadsmottaker er null ved sending av søknad. Dette skal ikke skje.")
            throw IllegalStateException("Soknad eller soknadsmottaker er null ved sending av søknad.")
        }

        val enhetsnummer = soknad.mottaker.enhetsnummer
        if (enhetsnummer == null) {
            log.error("Enhetsnummer er null ved sending av søknad. Den blir lagt til i tilleggsinformasjon-filen med <null> som verdi.")
        }
        val tilleggsinformasjonJson = JsonTilleggsinformasjon(enhetsnummer)

        return try {
            objectMapper.writeValueAsString(tilleggsinformasjonJson)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere tilleggsinformasjonJson", e)
        }
    }

    fun getVedleggJson(soknadUnderArbeid: SoknadUnderArbeid): String {
        return try {
            val vedleggJson = objectMapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad?.vedlegg)
            ensureValidVedlegg(vedleggJson)
            vedleggJson
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere vedleggJson", e)
        }
    }

    private fun convertToVedleggMetadataListe(soknadUnderArbeid: SoknadUnderArbeid): VedleggMetadataListe {
        val vedleggMetadataListe = VedleggMetadataListe()
        vedleggMetadataListe.vedleggListe = getVedleggFromInternalSoknad(soknadUnderArbeid)
            .map {
                VedleggMetadata(
                    skjema = it.type,
                    tillegg = it.tilleggsinfo,
                    filnavn = it.type,
                    status = Vedleggstatus.valueOf(it.status),
                )
            }.toMutableList()
        return vedleggMetadataListe
    }

    private fun slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid: SoknadUnderArbeid) {
        log.info("Sletter SoknadUnderArbeid, behandlingsid {}", soknadUnderArbeid.behandlingsId)
        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DigisosApiService::class.java)
    }
}
