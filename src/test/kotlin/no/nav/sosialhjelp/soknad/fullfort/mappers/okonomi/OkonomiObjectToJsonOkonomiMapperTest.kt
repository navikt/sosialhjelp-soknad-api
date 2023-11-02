package no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.repository.okonomi.createFullInntekt
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.IllegalArgumentException

class OkonomiObjectToJsonOkonomiMapperTest {


    @Test
    fun `Map Inntekt-domeneklasse med Opplysning-type til Json-klasse`() {

        val inntekt = createFullInntekt(UUID.randomUUID())
        val jsonOkonomi = JsonOkonomi()

        jsonOkonomi.mapFromDomainObject(inntekt)

        jsonOkonomi.additionalProperties
    }

    @Test
    fun `Mappe Inntekt-objekt med Opplysning-type og Utbetalint = null skal gi error`() {
        val inntekt = Inntekt(
            id = UUID.randomUUID(),
            soknadId = UUID.randomUUID(),
            type = InntektType.HUSBANKEN_VEDTAK,
        )

        assertThatThrownBy {
            JsonOkonomi().also { it.mapFromDomainObject(inntekt) }
        }.isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    fun `Map Inntekt-domeneklasse med Oversikt-type til Json-klasse`() {

        val inntekt = Inntekt (
            id = UUID.randomUUID(),
            soknadId = UUID.randomUUID(),
            type = InntektType.STUDENT_VEDTAK,
            tittel = "tittel",
            brutto = 480,
            netto = 250
        )
        val jsonOkonomi = JsonOkonomi()

        jsonOkonomi.mapFromDomainObject(inntekt)

        jsonOkonomi.additionalProperties
    }

    @Test
    fun `Map Utgift-domeneklasse med Opplysning-type til Json-klasse`() {

        val inntekt = createFullInntekt(UUID.randomUUID())
        val jsonOkonomi = JsonOkonomi()

        jsonOkonomi.mapFromDomainObject(inntekt)

        jsonOkonomi.additionalProperties
    }

    @Test
    fun `Map Utgift-domeneklasse med Oversikt-type til Json-klasse`() {

//        val inntekt = createFullInntekt(UUID.randomUUID())
//        val jsonOkonomi = JsonOkonomi()
//            .withOpplysninger(JsonOkonomiopplysninger())
//            .withOversikt(JsonOkonomioversikt())
//
//        mapper.mapByOkonomiType(inntekt, jsonOkonomi)
//
//        val opplysninger = jsonOkonomi.opplysninger
//        val oversikt = jsonOkonomi.oversikt
//        jsonOkonomi.additionalProperties
    }

    @Test
    fun `Map Formue-domeneklasse med Formye-type til Json-klasse`() {

//        val inntekt = createFullInntekt(UUID.randomUUID())
//        val jsonOkonomi = JsonOkonomi()
//            .withOpplysninger(JsonOkonomiopplysninger())
//            .withOversikt(JsonOkonomioversikt())
//
//        mapper.mapByOkonomiType(inntekt, jsonOkonomi)
//
//        val opplysninger = jsonOkonomi.opplysninger
//        val oversikt = jsonOkonomi.oversikt
//        jsonOkonomi.additionalProperties
    }
}
