package no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.UtgiftType
import no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi.type.toSoknadJsonType
import no.nav.sosialhjelp.soknad.repository.okonomi.createFullInntekt
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.IllegalArgumentException

class InntektUtgiftFormueMapperTest {


    @Test
    fun `Map Inntekt-domeneklasse med Opplysning-type til Json-klasse`() {

        val inntekt = createFullInntekt(UUID.randomUUID())
        val jsonOkonomi = JsonOkonomi().apply { mapFromDomainObject(inntekt) }

        assertThat(jsonOkonomi.opplysninger.utbetaling).hasSize(1)
        assertThat(jsonOkonomi.opplysninger.utbetaling.first().type).isEqualTo(inntekt.type.toSoknadJsonType())
    }

    @Test
    fun `Mappe Inntekt-objekt med Opplysning-type og Utbetaling = null skal gi error`() {
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
        val jsonOkonomi = JsonOkonomi().apply { mapFromDomainObject(inntekt) }

        assertThat(jsonOkonomi.oversikt.inntekt).hasSize(1)
        assertThat(jsonOkonomi.oversikt.inntekt.first().type).isEqualTo(inntekt.type.toSoknadJsonType())
    }

    @Test
    fun `Map Utgift-domeneklasse med Opplysning-type til Json-klasse`() {

        val utgift = Utgift(
            id = UUID.randomUUID(),
            soknadId = UUID.randomUUID(),
            type = UtgiftType.ANDRE_UTGIFTER
        )
        val jsonOkonomi = JsonOkonomi().also { it.mapFromDomainObject(utgift) }

        assertThat(jsonOkonomi.opplysninger.utgift).hasSize(1)
        assertThat(jsonOkonomi.opplysninger.utgift.first().type).isEqualTo(utgift.type.toSoknadJsonType())
    }

    @Test
    fun `Map Utgift-domeneklasse med Oversikt-type til Json-klasse`() {
        val utgift = Utgift(
            id = UUID.randomUUID(),
            soknadId = UUID.randomUUID(),
            type = UtgiftType.BARNEBIDRAG_BETALER,
            tittel = "tittel",
            belop = 480
        )
        val jsonOkonomi = JsonOkonomi().also { it.mapFromDomainObject(utgift) }

        assertThat(jsonOkonomi.oversikt.utgift).hasSize(1)
        assertThat(jsonOkonomi.oversikt.utgift.first().type).isEqualTo(utgift.type.toSoknadJsonType())
    }

    @Test
    fun `Map Formue-domeneklasse med Formye-type til Json-klasse`() {

        val formue = Formue(
            id = UUID.randomUUID(),
            soknadId = UUID.randomUUID(),
            type = FormueType.KONTOOVERSIKT_BRUKSKONTO,
            tittel = "tittel",
            belop = 480
        )
        val jsonOkonomi = JsonOkonomi().also { it.mapFromDomainObject(formue) }

        assertThat(jsonOkonomi.oversikt.formue).hasSize(1)
        assertThat(jsonOkonomi.oversikt.formue.first().type).isEqualTo(formue.type.toSoknadJsonType())
    }
}
