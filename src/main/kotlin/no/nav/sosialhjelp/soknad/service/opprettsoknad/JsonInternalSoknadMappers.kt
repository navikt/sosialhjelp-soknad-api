package no.nav.sosialhjelp.soknad.service.opprettsoknad

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.domene.soknad.Bosituasjon
import no.nav.sosialhjelp.soknad.domene.soknad.Fil
import no.nav.sosialhjelp.soknad.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.domene.soknad.Vedlegg
import java.time.format.DateTimeFormatter

object JsonInternalSoknadMappers {
    fun JsonInternalSoknad.map(soknad: Soknad) {

        this.soknad.data.begrunnelse
            .withHvorforSoke(soknad.hvorforSoke)
            .withHvaSokesOm(soknad.hvaSokesOm)

        this.soknad.withInnsendingstidspunkt(
            soknad.innsendingstidspunkt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?:
            SoknadUnderArbeidService.nowWithForcedNanoseconds()
        )
    }

    fun JsonInternalSoknad.map(bosituasjon: Bosituasjon) {
        this.soknad.data.bosituasjon
            .withBotype(JsonBosituasjon.Botype.valueOf(bosituasjon.botype!!.name))
            .withAntallPersoner(bosituasjon.antallPersoner)
    }

    fun JsonInternalSoknad.map(vedleggMedFiler: Pair<Vedlegg, List<Fil>>) {
        val (vedlegg, filer) = vedleggMedFiler
    }


}