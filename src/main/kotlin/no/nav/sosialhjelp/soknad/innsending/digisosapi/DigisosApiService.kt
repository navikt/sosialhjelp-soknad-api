package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.getVedleggFromInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.navKontorTilMetricNavn
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil.genererOgLoggVedleggskravStatistikk
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime

@Component
class DigisosApiService(
    private val digisosApiV2Client: DigisosApiV2Client,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val dokumentListeService: DokumentListeService,
    private val prometheusMetricsService: PrometheusMetricsService,
    private val clock: Clock
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun sendSoknad(soknadUnderArbeid: SoknadUnderArbeid, token: String?, kommunenummer: String): String {
        var behandlingsId = soknadUnderArbeid.behandlingsId
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke sende søknad hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid)
        log.info("Starter innsending av søknad med behandlingsId $behandlingsId, skal sendes til DigisosApi v2")
        val vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid)
        oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid)
        val filOpplastinger = dokumentListeService.getFilOpplastingList(soknadUnderArbeid)
        val soknadJson = getSoknadJson(soknadUnderArbeid)
        val tilleggsinformasjonJson = getTilleggsinformasjonJson(jsonInternalSoknad.soknad)
        val vedleggJson = getVedleggJson(soknadUnderArbeid)

        if (MiljoUtils.isNonProduction()) {
            behandlingsId = createPrefixedBehandlingsId(behandlingsId)
        }
        val enhetsnummer = jsonInternalSoknad.soknad.mottaker.enhetsnummer
        val navEnhetsnavn = jsonInternalSoknad.soknad.mottaker.navEnhetsnavn

        log.info("Starter kryptering av filer for $behandlingsId, skal sende til kommune $kommunenummer med enhetsnummer $enhetsnummer og navenhetsnavn $navEnhetsnavn")
        val digisosId = try {
            digisosApiV2Client.krypterOgLastOppFiler(
                soknadJson = soknadJson,
                tilleggsinformasjonJson = tilleggsinformasjonJson,
                vedleggJson = vedleggJson,
                dokumenter = filOpplastinger,
                kommunenr = kommunenummer,
                navEksternRefId = behandlingsId,
                token = token
            )
        } catch (e: Exception) {
            prometheusMetricsService.reportFeiletMedDigisosApi()
            throw e
        }

        genererOgLoggVedleggskravStatistikk(soknadUnderArbeid, vedlegg.vedleggListe)

        prometheusMetricsService.reportSendtMedDigisosApi()
        prometheusMetricsService.reportSoknadMottaker(soknadUnderArbeid.erEttersendelse, navKontorTilMetricNavn(navEnhetsnavn))
        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
        if (soknadMetadata == null) {
            log.info("soknadmetadata er null?")
        }
        soknadMetadata?.let {
            val tidBrukt = Duration.between(it.opprettetDato, it.innsendtDato)
            log.info("Måler tid brukt fra ${it.opprettetDato} til ${it.innsendtDato}")
            prometheusMetricsService.reportInnsendingTid(tidBrukt.seconds)
        }

        slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid)
        return digisosId
    }

    private fun oppdaterMetadataVedAvslutningAvSoknad(
        behandlingsId: String?,
        vedlegg: VedleggMetadataListe,
        soknadUnderArbeid: SoknadUnderArbeid
    ) {
        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
        soknadMetadata?.vedlegg = vedlegg
        soknadMetadata?.orgnr = soknadUnderArbeid.jsonInternalSoknad?.mottaker?.organisasjonsnummer
        soknadMetadata?.navEnhet = soknadUnderArbeid.jsonInternalSoknad?.mottaker?.navEnhetsnavn
        soknadMetadata?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadata?.innsendtDato = LocalDateTime.now(clock)
        soknadMetadata?.status = SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
        soknadMetadataRepository.oppdater(soknadMetadata)
        log.info("Søknad avsluttet $behandlingsId ${soknadMetadata?.skjema}, ${vedlegg.vedleggListe.size}")
    }

    private fun getSoknadJson(soknadUnderArbeid: SoknadUnderArbeid): String {
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

    private fun getVedleggJson(soknadUnderArbeid: SoknadUnderArbeid): String {
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
        log.info("Sletter SoknadUnderArbeid, behandlingsid ${soknadUnderArbeid.behandlingsId}")
        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DigisosApiService::class.java)
    }
}
