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
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MED_MANGLER
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadDuplicateFilename
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import org.springframework.stereotype.Component
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

        val filenameConflict =
            JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
                .any { it.filer.any { jsonFile -> jsonFile.filnavn == filnavn } }

        if (filenameConflict) throw DokumentUploadDuplicateFilename()
    }

    fun fjernVedleggFraInternalSoknad(
        behandlingsId: String,
        aktueltVedlegg: MellomlagringDokumentInfo,
    ) {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        val jsonVedlegg: JsonVedlegg =
            JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
                .firstOrNull { it.filer.any { jsonFil -> jsonFil.filnavn == aktueltVedlegg.filnavn } }
                ?: throw IkkeFunnetException("dokumentet ${aktueltVedlegg.filnavn} ikke funnet")

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
        innsendingsTidspunkt: String = nowWithForcedMillis(),
    ) {
        if (soknadUnderArbeid == null) {
            throw RuntimeException("Søknad under arbeid mangler")
        }

        soknadUnderArbeid.jsonInternalSoknad?.soknad?.innsendingstidspunkt = innsendingsTidspunkt
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

        val kommunenummer =
            soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
                ?: return false.also { log.info("Mottaker.kommunenummer ikke satt -> skalSoknadSendesMedDigisosApi returnerer false") }

        return when (kommuneInfoService.getKommuneStatus(kommunenummer)) {
            FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException(
                    "Mellomlagring av vedlegg er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.",
                )
            }

            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MED_MANGLER -> {
                throw SendingTilKommuneUtilgjengeligException("Kommune mangler eller har feil konfigurasjon")
            }

            SKAL_SENDE_SOKNADER_VIA_FDA -> true
            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException(
                    "Sending til kommune $kommunenummer er midlertidig utilgjengelig.",
                )
            }
        }
    }

    companion object {
        fun nowWithForcedMillis(now: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)): String {
            return now
                .run { if (nano == 0) plusNanos(1000000) else this }
                .truncatedTo(ChronoUnit.MILLIS).toString()
        }

        private val log by logger()
    }
}
