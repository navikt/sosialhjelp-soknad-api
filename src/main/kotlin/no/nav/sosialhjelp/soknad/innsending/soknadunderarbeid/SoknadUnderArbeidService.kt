package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SamtidigOppdateringException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class SoknadUnderArbeidService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val kommuneInfoService: KommuneInfoService
) {
    fun settInnsendingstidspunktPaSoknad(soknadUnderArbeid: SoknadUnderArbeid?) {
        if (soknadUnderArbeid == null) {
            throw RuntimeException("Søknad under arbeid mangler")
        }
        if (soknadUnderArbeid.erEttersendelse) {
            return
        }
        soknadUnderArbeid.jsonInternalSoknad?.soknad?.innsendingstidspunkt = nowWithForcedNanoseconds()
        try {
            // TODO EKSTRA LOGGING
            NavMessageSource.log.info(
                "${this::class.java.name} - Oppdaterer søknad under arbeid for ${soknadUnderArbeid.behandlingsId} - " +
                    "Versjon: ${soknadUnderArbeid.versjon}, " +
                    "Sist endret: ${soknadUnderArbeid.sistEndretDato}"
            )
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.eier)
            // TODO *** EKSTRA LOGGING
            NavMessageSource.log.info(
                "${this::class.java.name} - Søknad under arbeid er oppdatert for ${soknadUnderArbeid.behandlingsId} " +
                    "Versjon: ${soknadUnderArbeid.versjon}, " +
                    "Sist endret: ${soknadUnderArbeid.sistEndretDato}"
            )
        } catch (e: SamtidigOppdateringException) {
            NavMessageSource.log.error("${this::class.java.name} - ${e.message}")
        }
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

    fun skalSoknadSendesMedDigisosApi(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        if (soknadUnderArbeid.erEttersendelse) {
            return false
        }

        val kommunenummer = soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            ?: return false.also {
                log.info(
                    "BehandlingsId: ${soknadUnderArbeid.behandlingsId} - " +
                        "Mottaker.kommunenummer ikke satt -> skalSoknadSendesMedDigisosApi returnerer false"
                )
            }

        return when (kommuneInfoService.getKommuneStatus(kommunenummer)) {
            FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException(
                    "BehandlingsId: ${soknadUnderArbeid.behandlingsId} " +
                        "- Mellomlagring av vedlegg er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom."
                )
            }
            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> false
            SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> true
            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException(
                    "BehandlingsId: ${soknadUnderArbeid.behandlingsId} " +
                        "- Sending til kommune $kommunenummer er midlertidig utilgjengelig."
                )
            }
        }
    }

    companion object {
        fun nowWithForcedNanoseconds(): String {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            return if (now.nano == 0) {
                now.plusNanos(1000000).toString()
            } else now.toString()
        }

        private val log by logger()
    }
}
