package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
open class SoknadUnderArbeidService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    open fun settInnsendingstidspunktPaSoknad(soknadUnderArbeid: SoknadUnderArbeid?) {
        if (soknadUnderArbeid == null) {
            throw RuntimeException("Søknad under arbeid mangler")
        }
        if (soknadUnderArbeid.erEttersendelse) {
            return
        }
        soknadUnderArbeid.jsonInternalSoknad?.soknad?.innsendingstidspunkt = nowWithForcedNanoseconds()
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    open fun sortArbeid(arbeid: JsonArbeid) {
        if (arbeid.forhold != null) {
            arbeid.forhold.sortBy { it.arbeidsgivernavn }
        }
    }

    open fun sortOkonomi(okonomi: JsonOkonomi) {
        okonomi.opplysninger.bekreftelse.sortBy { it.type }
        okonomi.opplysninger.utbetaling.sortBy { it.type }
        okonomi.opplysninger.utgift.sortBy { it.type }
        okonomi.oversikt.inntekt.sortBy { it.type }
        okonomi.oversikt.utgift.sortBy { it.type }
        okonomi.oversikt.formue.sortBy { it.type }
    }

    companion object {
        fun nowWithForcedNanoseconds(): String {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            return if (now.nano == 0) {
                now.plusNanos(1000000).toString()
            } else now.toString()
        }
    }
}
