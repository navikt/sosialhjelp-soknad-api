package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.getVedleggFromInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.svarut.OppgaveHandterer
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteSystemdata
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil.genererOgLoggVedleggskravStatistikk
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.MINUTES

@Component
open class SoknadService(
    private val henvendelseService: HenvendelseService,
    private val oppgaveHandterer: OppgaveHandterer,
    private val innsendingService: InnsendingService,
    private val ettersendingService: EttersendingService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdataUpdater: SystemdataUpdater,
    private val bostotteSystemdata: BostotteSystemdata,
    private val skatteetatenSystemdata: SkatteetatenSystemdata,
    private val mellomlagringService: MellomlagringService,
    private val prometheusMetricsService: PrometheusMetricsService
) {
    @Transactional
    open fun startSoknad(token: String?): String {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val behandlingsId = henvendelseService.startSoknad(eier)

        prometheusMetricsService.reportStartSoknad(false)

        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = null,
            eier = eier,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        systemdataUpdater.update(soknadUnderArbeid)
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, eier)

        return behandlingsId
    }

    @Transactional
    open fun sendSoknad(behandlingsId: String) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        log.info("Starter innsending av søknad med behandlingsId $behandlingsId")
        logDriftsinformasjon(soknadUnderArbeid)

        validateEttersendelseHasVedlegg(soknadUnderArbeid)
        val vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid)

        henvendelseService.oppdaterMetadataVedAvslutningAvSvarUtSoknad(behandlingsId, vedlegg, soknadUnderArbeid)
        oppgaveHandterer.leggTilOppgave(behandlingsId, eier)
        innsendingService.oppdaterSoknadUnderArbeid(soknadUnderArbeid)

        genererOgLoggVedleggskravStatistikk(soknadUnderArbeid, vedlegg.vedleggListe)
    }

    private fun validateEttersendelseHasVedlegg(soknadUnderArbeid: SoknadUnderArbeid) {
        if (soknadUnderArbeid.erEttersendelse && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()) {
            log.error("Kan ikke sende inn ettersendingen med ID ${soknadUnderArbeid.behandlingsId} uten å ha lastet opp vedlegg")
            throw SosialhjelpSoknadApiException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg")
        }
    }

    private fun logDriftsinformasjon(soknadUnderArbeid: SoknadUnderArbeid) {
        if (!soknadUnderArbeid.erEttersendelse) {
            if (java.lang.Boolean.TRUE == soknadUnderArbeid.jsonInternalSoknad?.soknad?.driftsinformasjon?.stotteFraHusbankenFeilet) {
                val alderPaaData = finnAlderPaaDataFor(soknadUnderArbeid, BOSTOTTE_SAMTYKKE)
                log.info("Nedlasting fra Husbanken har feilet for innsendtsoknad. $alderPaaData")
            }
            if (java.lang.Boolean.TRUE == soknadUnderArbeid.jsonInternalSoknad?.soknad?.driftsinformasjon?.inntektFraSkatteetatenFeilet) {
                val alderPaaData = finnAlderPaaDataFor(soknadUnderArbeid, UTBETALING_SKATTEETATEN_SAMTYKKE)
                log.info("Nedlasting fra Skatteetaten har feilet for innsendtsoknad. $alderPaaData")
            }
        }
    }

    private fun finnAlderPaaDataFor(soknadUnderArbeid: SoknadUnderArbeid, type: String): String {
        val bekreftelsesDatoStreng =
            soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.okonomi?.opplysninger?.bekreftelse
                ?.firstOrNull { it.type == type && it.verdi }
                ?.bekreftelsesDato
                ?: return ""
        val bekreftelsesDato = OffsetDateTime.parse(bekreftelsesDatoStreng)
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val antallDager = bekreftelsesDato.until(now, DAYS)
        val antallTimer = bekreftelsesDato.until(now, HOURS) % 24
        val antallMinutter = bekreftelsesDato.until(now, MINUTES) % 60
        return " Dataene er $antallDager dager, $antallTimer timer og $antallMinutter minutter gamle."
    }

    @Transactional
    open fun avbrytSoknad(behandlingsId: String) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        soknadUnderArbeidRepository.hentSoknadNullable(behandlingsId, eier)
            ?.let { soknadUnderArbeid ->
                if (mellomlagringService.kanSoknadHaMellomlagredeVedleggForSletting(soknadUnderArbeid)) {
                    mellomlagringService.deleteAllVedlegg(behandlingsId)
                }
                soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, eier)
                henvendelseService.avbrytSoknad(soknadUnderArbeid.behandlingsId, false)
                prometheusMetricsService.reportAvbruttSoknad(soknadUnderArbeid.erEttersendelse)
            }
    }

    @Transactional
    open fun oppdaterSamtykker(
        behandlingsId: String?,
        harBostotteSamtykke: Boolean,
        harSkatteetatenSamtykke: Boolean,
        token: String?,
    ) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        if (harSkatteetatenSamtykke) {
            skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        }
        if (harBostotteSamtykke) {
            bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, token)
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    open fun startEttersending(behandlingsIdSoknad: String?): String {
        return ettersendingService.start(behandlingsIdSoknad)
    }

    private fun convertToVedleggMetadataListe(soknadUnderArbeid: SoknadUnderArbeid): VedleggMetadataListe {
        val jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid)
        val vedlegg = VedleggMetadataListe()
        vedlegg.vedleggListe = jsonVedleggs.map { mapJsonVedleggToVedleggMetadata(it) }.toMutableList()
        return vedlegg
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadService::class.java)

        fun createEmptyJsonInternalSoknad(eier: String): JsonInternalSoknad {
            return JsonInternalSoknad().withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withPersonalia(
                                JsonPersonalia()
                                    .withPersonIdentifikator(
                                        JsonPersonIdentifikator()
                                            .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                            .withVerdi(eier)
                                    )
                                    .withNavn(
                                        JsonSokernavn()
                                            .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                            .withFornavn("")
                                            .withMellomnavn("")
                                            .withEtternavn("")
                                    )
                                    .withKontonummer(
                                        JsonKontonummer()
                                            .withKilde(JsonKilde.SYSTEM)
                                    )
                            )
                            .withArbeid(JsonArbeid())
                            .withUtdanning(
                                JsonUtdanning()
                                    .withKilde(JsonKilde.BRUKER)
                            )
                            .withFamilie(
                                JsonFamilie()
                                    .withForsorgerplikt(JsonForsorgerplikt())
                            )
                            .withBegrunnelse(
                                JsonBegrunnelse()
                                    .withKilde(JsonKildeBruker.BRUKER)
                                    .withHvorforSoke("")
                                    .withHvaSokesOm("")
                            )
                            .withBosituasjon(
                                JsonBosituasjon()
                                    .withKilde(JsonKildeBruker.BRUKER)
                            )
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(
                                        JsonOkonomiopplysninger()
                                            .withUtbetaling(ArrayList())
                                            .withUtgift(ArrayList())
                                            .withBostotte(JsonBostotte())
                                            .withBekreftelse(ArrayList())
                                    )
                                    .withOversikt(
                                        JsonOkonomioversikt()
                                            .withInntekt(ArrayList())
                                            .withUtgift(ArrayList())
                                            .withFormue(ArrayList())
                                    )
                            )
                    )
                    .withMottaker(
                        JsonSoknadsmottaker()
                            .withNavEnhetsnavn("")
                            .withEnhetsnummer("")
                    )
                    .withDriftsinformasjon(
                        JsonDriftsinformasjon()
                            .withUtbetalingerFraNavFeilet(false)
                            .withInntektFraSkatteetatenFeilet(false)
                            .withStotteFraHusbankenFeilet(false)
                    )
                    .withKompatibilitet(ArrayList())
            ).withVedlegg(JsonVedleggSpesifikasjon())
        }

        private fun mapJsonVedleggToVedleggMetadata(jsonVedlegg: JsonVedlegg): VedleggMetadata {
            return VedleggMetadata(
                skjema = jsonVedlegg.type,
                tillegg = jsonVedlegg.tilleggsinfo,
                filnavn = jsonVedlegg.type,
                status = Vedleggstatus.valueOf(jsonVedlegg.status),
                hendelseType = jsonVedlegg.hendelseType,
                hendelseReferanse = jsonVedlegg.hendelseReferanse,

            )
        }
    }
}
