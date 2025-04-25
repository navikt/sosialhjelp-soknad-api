package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.okonomi.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.junit.jupiter.api.Test

class SoknadJsonTypeEnumTest {
    val listOfInntekter: MutableList<Inntekt> = mutableListOf()
    val listOfUtgift: MutableList<Utgift> = mutableListOf()
    val listOfFormue: MutableList<Formue> = mutableListOf()

    @Test
    fun `VedleggTyper skal mappes til OpplysningTyper`() {
        VedleggType.entries.forEach { type ->

            type.opplysningType?.let {
                when (it) {
                    is InntektType -> createInntekt(it)
                    is FormueType -> createFormue(it)
                    is UtgiftType -> createUtgift(it)
                    else -> {
                        null
                    }
                }
            }
        }
    }

    private fun createInntekt(opplysningType: InntektType) {
        Inntekt(
            type = opplysningType,
        )
            .also { listOfInntekter.add(it) }
    }

    private fun createFormue(opplysningType: FormueType) {
        Formue(
            type = opplysningType,
        )
            .also { listOfFormue.addLast(it) }
    }

    private fun createUtgift(opplysningType: UtgiftType) {
        Utgift(
            type = opplysningType,
        )
            .also { listOfUtgift.addLast(it) }
    }
}
