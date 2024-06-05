package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.AbstractOkonomiServiceTest
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class FormueServiceTest : AbstractOkonomiServiceTest() {
    @Autowired
    private lateinit var formueService: FormueService

    @Test
    fun `Oppdatere formue skal generere formue-objekt, bekreftelse og vedlegg`() {
        FormueInput(hasBrukskonto = true)
            .also { formueService.updateFormuer(soknad.id, it) }

        with(okonomiRepository.findByIdOrNull(soknad.id)!!) {
            assertThat(formuer).hasSize(1)
            assertThat(formuer).anyMatch { it.type == FormueType.FORMUE_BRUKSKONTO }
            assertThat(bekreftelser).hasSize(1)
            assertThat(bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_SPARING }
        }

        with(dokumentasjonRepository.findAllBySoknadId(soknad.id)) {
            assertThat(this).hasSize(1)
            assertThat(this.any { it.type == FormueType.FORMUE_BRUKSKONTO }).isTrue()
        }
    }

    @Test
    fun `Hente formuer skal kun returnere relevante FormueTyper`() {
        val formueType = FormueType.FORMUE_BRUKSKONTO
        val verdiType = FormueType.VERDI_KJORETOY
        Okonomi(
            soknadId = soknad.id,
            formuer = setOf(Formue(formueType), Formue(verdiType)),
        ).also { okonomiRepository.save(it) }

        with(formueService.getFormuer(soknad.id)) {
            assertThat(this).hasSize(1)
            assertThat(this).anyMatch { it.type == formueType }
            assertThat(this).noneMatch { it.type == verdiType }
        }
    }

    @Test
    fun `Fjerne Formue skal slette vedlegg`() {
        FormueInput(hasBrukskonto = true).also { formueService.updateFormuer(soknad.id, it) }

        with(dokumentasjonRepository.findAllBySoknadId(soknad.id)) {
            assertThat(any { it.type == FormueType.FORMUE_BRUKSKONTO }).isTrue()
        }
        // Alle felter false
        FormueInput().also { formueService.updateFormuer(soknad.id, it) }

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.formuer).hasSize(0)
        assertThat(dokumentasjonRepository.findAllBySoknadId(soknad.id)).hasSize(0)
    }

    @Test
    fun `Legge til beskrivelse skal generere 1 formue-objekt, 1 bekreftelse og 1 vedlegg`() {
        val beskrivelseString = "Beskrivelse av annet"
        FormueInput(beskrivelseSparing = beskrivelseString).also { formueService.updateFormuer(soknad.id, it) }
        val formueType = FormueType.FORMUE_ANNET

        with(okonomiRepository.findByIdOrNull(soknad.id)!!) {
            assertThat(formuer).hasSize(1)
            assertThat(formuer).anyMatch { it.type == formueType }
            assertThat(bekreftelser).hasSize(1)
            assertThat(bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_SPARING }

            assertThat(formuer.find { it.type == FormueType.FORMUE_ANNET }?.beskrivelse).isEqualTo(beskrivelseString)
        }

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it).hasSize(1)
            assertThat(it).anyMatch { vedlegg -> vedlegg.type == formueType }
        }
    }

    @Test
    fun `Endre beskrivelse skal oppdateres`() {
        val beskrivelseString = "Beskrivelse av annet"

        FormueInput(
            hasBeskrivelseSparing = true,
            beskrivelseSparing = beskrivelseString,
        ).also { formueService.updateFormuer(soknad.id, it) }
        okonomiRepository.findByIdOrNull(soknad.id)!!.formuer.run {
            assertThat(toList()).hasSize(1).allMatch { it.beskrivelse == beskrivelseString }
        }

        FormueInput(
            hasBeskrivelseSparing = true,
            beskrivelseSparing = null,
        ).also { formueService.updateFormuer(soknad.id, it) }
        okonomiRepository.findByIdOrNull(soknad.id)!!.formuer.run {
            assertThat(toList()).hasSize(1).allMatch { it.beskrivelse == null }
        }
    }
}
