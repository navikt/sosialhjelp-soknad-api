package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.AbstractOkonomiServiceTest
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class VerdiServiceTest : AbstractOkonomiServiceTest() {
    @Autowired
    private lateinit var verdiService: VerdiService

    @Test
    fun `Oppdatere Verdi skal generere formue-objekt og bekreftelse, men ikke vedlegg`() {
        HarVerdierInput(hasBolig = true).also { verdiService.updateVerdier(soknad.id, it) }

        with(okonomiRepository.findByIdOrNull(soknad.id)!!) {
            assertThat(formuer).hasSize(1)
            assertThat(formuer).anyMatch { it.type == FormueType.VERDI_BOLIG }
            assertThat(bekreftelser).hasSize(1)
            assertThat(bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_VERDI }
        }

        assertThat(vedleggRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    @Test
    fun `Hente verdier skal kun returnere relevante FormueTyper`() {
        val formueType = FormueType.FORMUE_BRUKSKONTO
        val verdiType = FormueType.VERDI_KJORETOY
        Okonomi(
            soknadId = soknad.id,
            formuer = listOf(Formue(formueType), Formue(verdiType)),
        ).also { okonomiRepository.save(it) }

        with(verdiService.getVerdier(soknad.id)) {
            assertThat(this).hasSize(1)
            assertThat(this).anyMatch { it.type == verdiType }
            assertThat(this).noneMatch { it.type == formueType }
        }
    }

    @Test
    fun `Legge til beskrivelse skal generere formue-objekt og bekreftelse, men ikke vedlegg`() {
        val beskrivelseVerdi = "Beskrivelse av verdi"
        HarVerdierInput(beskrivelseVerdi = beskrivelseVerdi).also { verdiService.updateVerdier(soknad.id, it) }

        with(okonomiRepository.findByIdOrNull(soknad.id)!!) {
            assertThat(formuer).hasSize(1)
            assertThat(formuer).anyMatch { it.type == FormueType.VERDI_ANNET }
            assertThat(bekreftelser).hasSize(1)
            assertThat(bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_VERDI }
            assertThat(beskrivelserAnnet.verdi).isEqualTo(beskrivelseVerdi)
        }
        assertThat(vedleggRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    @Test
    fun `Sette bekreftelse false skal fjerne alle innslag`() {
        HarVerdierInput(hasBolig = true).also { verdiService.updateVerdier(soknad.id, it) }
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.formuer).hasSize(1)

        verdiService.removeVerdier(soknad.id)
        with(okonomiRepository.findByIdOrNull(soknad.id)!!) {
            assertThat(formuer).isEmpty()
            assertThat(bekreftelser).hasSize(1)
            assertThat(bekreftelser).anyMatch {
                it.type == BekreftelseType.BEKREFTELSE_VERDI && !it.verdi
            }
        }
    }
}
