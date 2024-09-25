package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.OkonomiToJsonHandler
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.toTittel
import no.nav.sosialhjelp.soknad.v2.json.getSoknadJsonTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class OkonomiMapperTest {
    private val mapper = OkonomiToJsonHandler.Mapper

    @Test
    fun `Bostotte special case skal opprette ett element med kilde == Bruker i Utbetalinger`() {
        val json = createJsonInternalSoknadWithInitializedSuperObjects()
        json.soknad.withData(
            JsonData().withOkonomi(
                JsonOkonomi()
                    .withOversikt(JsonOkonomioversikt())
                    .withOpplysninger(JsonOkonomiopplysninger()),
            ),
        )

        Okonomi(
            soknadId = UUID.randomUUID(),
            bekreftelser =
                setOf(
                    Bekreftelse(type = BekreftelseType.BOSTOTTE, verdi = true),
                    Bekreftelse(type = BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = false),
                ),
        ).let { mapper.doMapping(it, json.soknad.data.okonomi) }

        json.soknad.data.okonomi.opplysninger.also { opplysninger ->

            assertThat(opplysninger.utbetaling).hasSize(1)

            opplysninger.utbetaling.first()
                .let {
                    assertThat(it.type).isEqualTo(InntektType.UTBETALING_HUSBANKEN.getSoknadJsonTypeString())
                    assertThat(it.tittel).isEqualTo(BekreftelseType.BOSTOTTE.toTittel())
                    assertThat(it.utbetalingsdato).isNotNull()
                    assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER)
                }
        }
    }
}
