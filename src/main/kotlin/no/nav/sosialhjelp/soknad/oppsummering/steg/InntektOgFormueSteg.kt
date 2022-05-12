package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.AndreInntekter
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.AnnenFormue
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.Bank
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.BostotteHusbanken
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.NavUtbetalinger
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.SkattbarInntekt
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.Studielan

class InntektOgFormueSteg {
    private val skattbarInntektAvsnitt = SkattbarInntekt()
    private val navUtbetalinger = NavUtbetalinger()
    private val bostotteHusbanken = BostotteHusbanken()
    private val studielan = Studielan()
    private val andreInntekter = AndreInntekter()
    private val bank = Bank()
    private val annenFormue = AnnenFormue()

    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val okonomi = jsonInternalSoknad.soknad.data.okonomi
        val opplysninger = okonomi.opplysninger
        val driftsinformasjon = jsonInternalSoknad.soknad.driftsinformasjon

        val avsnitt = mutableListOf<Avsnitt>()
        avsnitt.add(skattbarInntektAvsnitt.getAvsnitt(okonomi, driftsinformasjon))
        avsnitt.add(navUtbetalinger.getAvsnitt(opplysninger, driftsinformasjon))
        avsnitt.add(bostotteHusbanken.getAvsnitt(opplysninger, driftsinformasjon))
        if (erStudent(jsonInternalSoknad.soknad.data.utdanning)) {
            avsnitt.add(studielan.getAvsnitt(opplysninger))
        }
        avsnitt.add(andreInntekter.getAvsnitt(opplysninger))
        avsnitt.add(bank.getAvsnitt(okonomi))
        avsnitt.add(annenFormue.getAvsnitt(okonomi))

        return Steg(
            stegNr = 6,
            tittel = "inntektbolk.tittel",
            avsnitt = avsnitt
        )
    }

    private fun erStudent(utdanning: JsonUtdanning?): Boolean {
        return utdanning != null && utdanning.erStudent != null && utdanning.erStudent == java.lang.Boolean.TRUE
    }
}
