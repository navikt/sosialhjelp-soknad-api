package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DuplikatFilException
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@Component
class SoknadUnderArbeidService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val kommuneInfoService: KommuneInfoService,
) {
    fun sjekkDuplikate(
        behandlingsId: String,
        filnavn: String,
    ) {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        val jsonVedlegg =
            JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
                .firstOrNull {
                    it.filer.any { jsonFile -> jsonFile.filnavn == filnavn }
                }
        if (jsonVedlegg != null) {
            throw DuplikatFilException("Fil finnes allerede.")
        }
    }

    fun fjernVedleggFraInternalSoknad(
        behandlingsId: String,
        aktueltVedlegg: MellomlagringDokumentInfo,
    ) {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        val jsonVedlegg: JsonVedlegg =
            JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
                .firstOrNull {
                    it.filer.any { jsonFil -> jsonFil.filnavn == aktueltVedlegg.filnavn }
                }
                ?: throw IkkeFunnetException(
                    "Dette vedlegget tilhører en utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?",
                )

        jsonVedlegg.filer.removeIf { it.filnavn == aktueltVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier())
    }

    fun oppdaterSoknadUnderArbeid(
        sha512: String,
        behandlingsId: String,
        vedleggstype: String,
        filnavn: String,
    ) {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        val jsonVedlegg = VedleggUtils.finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)

        if (jsonVedlegg.filer == null) {
            jsonVedlegg.filer = ArrayList()
        }
        jsonVedlegg
            .withStatus(Vedleggstatus.LastetOpp.name)
            .filer.add(
                JsonFiler()
                    .withFilnavn(filnavn)
                    .withSha512(sha512),
            )
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier())
    }

    fun settInnsendingstidspunktPaSoknad(
        soknadUnderArbeid: SoknadUnderArbeid?,
        innsendingsTidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    ) {
        if (soknadUnderArbeid == null) {
            throw RuntimeException("Søknad under arbeid mangler")
        }
        if (soknadUnderArbeid.erEttersendelse) {
            return
        }
        val innsendingString = OffsetDateTime.of(innsendingsTidspunkt, ZoneOffset.UTC).toString()
        // TODO Logging i forbindelse med feil klokkeslett
        log.info("Innsendingstidspunkt utgangspunkt: $innsendingsTidspunkt")
        log.info("Innsendingstidspunkt formatert: $innsendingString")

        soknadUnderArbeid.jsonInternalSoknad?.soknad?.innsendingstidspunkt = innsendingString
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    fun sortArbeid(arbeid: JsonArbeid) {
        if (arbeid.forhold != null) {
            arbeid.forhold.sortBy { it.arbeidsgivernavn }
        }
    }

    fun sortOkonomi(okonomi: JsonOkonomi) {
        okonomi.opplysninger.bekreftelse.sortBy { it.type }
        okonomi.opplysninger.utbetaling.sortBy { it.type }
        okonomi.opplysninger.utgift.sortBy { it.type }
        okonomi.oversikt.inntekt.sortBy { it.type }
        okonomi.oversikt.utgift.sortBy { it.type }
        okonomi.oversikt.formue.sortBy { it.type }
    }

    fun skalSoknadSendesMedDigisosApi(behandlingsId: String): Boolean {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        if (soknadUnderArbeid.erEttersendelse) {
            return false
        }

        val kommunenummer =
            soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
                ?: return false.also { log.info("Mottaker.kommunenummer ikke satt -> skalSoknadSendesMedDigisosApi returnerer false") }

        return when (kommuneInfoService.getKommuneStatus(kommunenummer)) {
            FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException(
                    "Mellomlagring av vedlegg er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.",
                )
            }
            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> false
            SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> true
            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException(
                    "Sending til kommune $kommunenummer er midlertidig utilgjengelig.",
                )
            }
        }
    }

    companion object {
        fun nowWithForcedNanoseconds(): String {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            return if (now.nano == 0) {
                now.plusNanos(1000000).toString()
            } else {
                now.toString()
            }
        }

        private val log by logger()
    }
}
