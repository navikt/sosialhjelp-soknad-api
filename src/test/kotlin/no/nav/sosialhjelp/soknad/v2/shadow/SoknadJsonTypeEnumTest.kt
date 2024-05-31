package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadJsonTypeEnum
import org.junit.jupiter.api.Test

class SoknadJsonTypeEnumTest {
    @Test
    fun `SoknadJsonTyper fra filformat skal gi SoknadJsonType`() {
        val type1 = SoknadJsonTypeEnum.getSoknadJsonType(SoknadJsonTyper.VERDI_ANNET)
        val type2 = SoknadJsonTypeEnum.getSoknadJsonType(SoknadJsonTyper.FORMUE_LIVSFORSIKRING)

        val a = 4
    }

    val listOfInntekter: MutableList<Inntekt> = mutableListOf()
    val listOfUtgift: MutableList<Utgift> = mutableListOf()
    val listOfFormue: MutableList<Formue> = mutableListOf()

    @Test
    fun `VedleggTyper skal mappes til OkonomiTyper`() {
        VedleggType.entries.forEach { type ->

            type.okonomiType?.let {
                when (it) {
                    is InntektType -> createInntekt(it)
                    is FormueType -> createFormue(it)
                    is UtgiftType -> createUtgift(it)
                }
            }
        }
        val a = 4
    }

    private fun createInntekt(okonomiType: InntektType) {
        Inntekt(
            type = okonomiType,
            tittel = "Inntekt av typen ${okonomiType.name}",
        )
            .also { listOfInntekter.add(it) }
    }

    private fun createFormue(okonomiType: FormueType) {
        Formue(
            type = okonomiType,
            tittel = "Formue av typen: ${okonomiType.name}",
        )
            .also { listOfFormue.addLast(it) }
    }

    private fun createUtgift(okonomiType: UtgiftType) {
        Utgift(
            type = okonomiType,
            tittel = "Utgift av typen: ${okonomiType.name}",
        )
            .also { listOfUtgift.addLast(it) }
    }
}
