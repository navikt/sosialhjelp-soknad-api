package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData.Soknadstype
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.getVedleggFromInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.navKontorTilMetricNavn
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil.genererOgLoggVedleggskravStatistikk
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Component
class DigisosApiService(
    private val digisosApiV2Client: DigisosApiV2Client,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val dokumentListeService: DokumentListeService,
    private val prometheusMetricsService: PrometheusMetricsService,
    private val clock: Clock,
    private val mellomlagringService: MellomlagringService,
    private val kodeverkService: KodeverkService,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun sendSoknad(
        soknadUnderArbeid: SoknadUnderArbeid,
        token: String?,
        kommunenummer: String,
    ): String {
        val behandlingsId = soknadUnderArbeid.behandlingsId
        val jsonInternalSoknad =
            soknadUnderArbeid.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke sende søknad hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        val innsendingsTidspunkt = SoknadUnderArbeidService.nowWithForcedMillis()
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid, innsendingsTidspunkt)

        jsonInternalSoknad.humanifyHvaSokesOm()

        log.info("Starter innsending av søknad")
        // Opprettes, lagres på metadata og brukes til statistikk - er ikke en del av forsendelsen?
        val vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid)
        // Oppdaterer metadata i lokal database
        oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid)
        // lagDokumentForSaksbehandlerPdf(internalSoknad), lagDokumentForJuridiskPdf(internalSoknad), lagDokumentForBrukerkvitteringPdf()
        // Logger også ut hvor mange mellomlagrede vedlegg som finnes
        val filOpplastinger = dokumentListeService.getFilOpplastingList(soknadUnderArbeid)
        // Json-streng av JsonSoknad-objektet
        val soknadJson = getSoknadJson(soknadUnderArbeid)
        // Sjekker at JsonSoknad, JsonMottaker og JsonMottaker.enhetsnummer finnes - kun et wrapper-objekt for enhetsnummer
        val tilleggsinformasjonJson = getTilleggsinformasjonJson(jsonInternalSoknad.soknad)

        // Siste kontroll på at vi ikke sender referanser til filer som ikke finnes i fiks
        syncVedleggMedMellomlagredeFiler(
            mellomlagretFiks = mellomlagringService.getAllVedlegg(behandlingsId),
            json = jsonInternalSoknad,
        )

        // JsonVedleggSpesifikasjon - brukes av FSL som oppslagsinfo mot mellomlagring
        val vedleggJson = getVedleggJson(jsonInternalSoknad)

        val enhetsnummer = jsonInternalSoknad.soknad.mottaker.enhetsnummer
        val navEnhetsnavn = jsonInternalSoknad.soknad.mottaker.navEnhetsnavn

        log.info(
            "Starter kryptering av filer for $behandlingsId, skal sende til kommune $kommunenummer med enhetsnummer $enhetsnummer og navenhetsnavn $navEnhetsnavn",
        )
        val digisosId =
            try {
                digisosApiV2Client.krypterOgLastOppFiler(
                    soknadJson = soknadJson,
                    tilleggsinformasjonJson = tilleggsinformasjonJson,
                    vedleggJson = vedleggJson,
                    dokumenter = filOpplastinger,
                    kommunenr = kommunenummer,
                    navEksternRefId = behandlingsId,
                    token = token,
                )
            } catch (e: Exception) {
                prometheusMetricsService.reportFeilet()
                throw e
            }

        genererOgLoggVedleggskravStatistikk(vedlegg.vedleggListe)

        val kommunenavn = kodeverkService.getKommunenavn(kommunenummer)

        prometheusMetricsService.reportSendt(jsonInternalSoknad.soknad.data.soknadstype == Soknadstype.KORT, kommunenavn)
        prometheusMetricsService.reportSoknadMottaker(navKontorTilMetricNavn(navEnhetsnavn))

        slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid)

        return digisosId
    }

    fun getSoknaderForUser(token: String): List<DigisosSak> = digisosApiV2Client.getSoknader(token)

    fun getSoknaderMedStatusMotattFagsystem(digisosIdListe: List<UUID>): List<UUID> {
        if (digisosIdListe.isEmpty()) {
            return emptyList()
        } else {
            return digisosApiV2Client
                .getStatusForSoknader(digisosIdListe).statusListe
                .filter { it.levertFagsystem == true }
                .map { it.digisosId }
        }
    }

    fun getInnsynsfilForSoknad(
        fiksDigisosId: String,
        dokumentId: String,
        token: String,
    ): JsonDigisosSoker = digisosApiV2Client.getInnsynsfil(fiksDigisosId, dokumentId, token)

    // Instant.now().toEpochMilli()
    fun getTimestampSistSendtSoknad(token: String): Long? =
        digisosApiV2Client
            .getSoknader(token)
            .filter { it.originalSoknadNAV != null }
            .sortedByDescending { it.originalSoknadNAV?.timestampSendt }
            .firstNotNullOfOrNull { it.originalSoknadNAV?.timestampSendt }

    private fun oppdaterMetadataVedAvslutningAvSoknad(
        behandlingsId: String?,
        vedlegg: VedleggMetadataListe,
        soknadUnderArbeid: SoknadUnderArbeid,
    ) {
        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
        soknadMetadata?.vedlegg = vedlegg
        soknadMetadata?.orgnr = soknadUnderArbeid.jsonInternalSoknad?.mottaker?.organisasjonsnummer
        soknadMetadata?.navEnhet = soknadUnderArbeid.jsonInternalSoknad?.mottaker?.navEnhetsnavn
        soknadMetadata?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadata?.innsendtDato = LocalDateTime.now(clock)
        soknadMetadata?.status = SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API

        soknadMetadata?.let {
            val tidBrukt = Duration.between(it.opprettetDato, it.innsendtDato)
            prometheusMetricsService.reportInnsendingTid(tidBrukt.seconds)
        }

        soknadMetadataRepository.oppdater(soknadMetadata)
        log.info("Oppdaterer metadata ved avslutning av søknad. ${soknadMetadata?.skjema}, ${vedlegg.vedleggListe.size}")
    }

    private fun getSoknadJson(soknadUnderArbeid: SoknadUnderArbeid): String =
        try {
            val soknadJson = objectMapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad?.soknad)
            ensureValidSoknad(soknadJson)
            soknadJson
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere sonadJson", e)
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

    private fun syncVedleggMedMellomlagredeFiler(
        mellomlagretFiks: List<MellomlagretVedleggMetadata>,
        json: JsonInternalSoknad,
    ) {
        log.info("Synkroniserer vedlegg.filer med mellomlager hos FIKS")

        val vedleggSpesifikasjon = json.vedlegg
        vedleggSpesifikasjon.vedlegg.forEach { vedlegg ->
            vedlegg.evaluateStatus()
            if (vedlegg.status == Vedleggstatus.LastetOpp.toString()) vedlegg.checkFiles(mellomlagretFiks)
        }
    }

    private fun JsonVedlegg.evaluateStatus() {
        if (filer.isNotEmpty() && status != VedleggStatus.LastetOpp.toString()) {
            log.warn("Vedlegg har status $status, men har filer. Fjerner filer.")
            filer = emptyList()
        }
    }

    private fun JsonVedlegg.checkFiles(mellomlagretFiks: List<MellomlagretVedleggMetadata>) {
        if (status != Vedleggstatus.LastetOpp.toString()) return
        // sjekker at fil finnes hos Mellomlager
        filer =
            filer.mapNotNull { fil ->
                mellomlagretFiks
                    .find { it.filnavn == fil.filnavn }
                    .let {
                        if (it != null) {
                            fil
                        } else {
                            log.error("Ved sending av søknad finnes ikke fil hos mellomlager. Fjerner")
                            null
                        }
                    }
            }
        // endre status til kreves hvis den tidligere var LastetOpp, men ingen registrerte filer
        if (filer.isEmpty()) status = Vedleggstatus.VedleggKreves.toString()
    }

    private fun getVedleggJson(json: JsonInternalSoknad): String =
        try {
            objectMapper.writeValueAsString(json.vedlegg).also { ensureValidVedlegg(it) }
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere vedleggJson", e)
        }

    private fun convertToVedleggMetadataListe(soknadUnderArbeid: SoknadUnderArbeid): VedleggMetadataListe {
        val vedleggMetadataListe = VedleggMetadataListe()
        vedleggMetadataListe.vedleggListe =
            getVedleggFromInternalSoknad(soknadUnderArbeid)
                .filter { (it.type != "kort" || it.filer.isNotEmpty()) && it.status != null }
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
        log.info("Sletter SoknadUnderArbeid")
        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DigisosApiService::class.java)
    }
}

internal fun JsonInternalSoknad.humanifyHvaSokesOm() {
    val hvaSokesOm =
        soknad
            ?.data
            ?.begrunnelse
            ?.hvaSokesOm

    val humanifiedText = hvaSokesOm?.let { BegrunnelseUtils.jsonToHvaSokesOm(it) }

    val result =
        when {
            hvaSokesOm == null -> ""
            // Hvis ingen kategorier er valgt
            hvaSokesOm == "[]" -> ""
            // Hvis det er "vanlig" tekst i feltet
            humanifiedText == null -> hvaSokesOm
            // Hvis det er json-tekst
            else -> humanifiedText
        }

    soknad
        ?.data
        ?.begrunnelse
        ?.hvaSokesOm = result
}
