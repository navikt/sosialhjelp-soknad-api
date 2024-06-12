package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueDto
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarIkkeVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdierDto
import no.nav.sosialhjelp.soknad.v2.opprettOkonomi
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class FormueIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiRepository: OkonomiRepository

    @Autowired
    private lateinit var dokumentasjonsRepository: DokumentasjonRepository

    @Test
    fun `Hente Formuer skal returnere FormueDto med korrekte flagg`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val okonomi = opprettOkonomi(soknad.id).let { okonomiRepository.save(it) }

        doGet(
            uri = getFormueUrl(soknad.id),
            responseBodyClass = FormueDto::class.java,
        )
            .also { dto -> dto.assertFormuer(okonomi.formuer) }
    }

    @Test
    fun `Ingen formuer skal returnere Dto med alle flagg satt til false`() {
        val soknad = soknadRepository.save(opprettSoknad())

        doGet(
            uri = getFormueUrl(soknad.id),
            FormueDto::class.java,
        )
            .also { dto ->
                assertThat(
                    listOf(
                        dto.hasBrukskonto,
                        dto.hasSparekonto,
                        dto.hasBsu,
                        dto.hasVerdipapirer,
                        dto.hasLivsforsikring,
                        dto.hasSparing,
                    ),
                ).allMatch { it == false }

                assertThat(dto.beskrivelseSparing).isNull()
            }
    }

    @Test
    fun `Oppdatere ANNET med tomt object fjerner element`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val formue =
            Formue(
                type = FormueType.FORMUE_ANNET,
                beskrivelse = "Beskrivelse av Sparing",
            )
        formue.also {
            okonomiRepository.save(opprettOkonomi(soknad.id).copy(formuer = setOf(it)))
        }

        doGet(
            uri = getFormueUrl(soknad.id),
            FormueDto::class.java,
        ).also {
            assertThat(it.hasSparing).isTrue()
            assertThat(it.beskrivelseSparing).isEqualTo(formue.beskrivelse)
        }

        doPut(
            uri = getFormueUrl(soknad.id),
            requestBody = FormueInput(hasBeskrivelseSparing = false),
            responseBodyClass = FormueDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.formuer).isEmpty()
        }
    }

    @Test
    fun `Hente formue med beskrivelse skal eksistere i Dto`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val okonomi =
            Formue(
                type = FormueType.FORMUE_ANNET,
                beskrivelse = "Beskrivelse av Sparing",
            )
                .let { formue -> opprettOkonomi(soknad.id).copy(formuer = setOf(formue)) }
                .let { okonomi -> okonomiRepository.save(okonomi) }

        val formueDto =
            doGet(
                uri = getFormueUrl(soknad.id),
                FormueDto::class.java,
            )
                .also { dto -> dto.assertFormuer(okonomi.formuer) }

        okonomi.formuer.find { it.type == FormueType.FORMUE_ANNET }
            ?.let { formue ->
                assertThat(formueDto.beskrivelseSparing).isNotNull().isEqualTo(formue.beskrivelse)
            }
    }

    @Test
    fun `Hente Verdier skal returnere VerdierDto med korrekte flagg`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val okonomi = opprettOkonomi(soknad.id).let { okonomiRepository.save(it) }

        doGet(
            uri = getVerdiUrl(soknad.id),
            responseBodyClass = VerdierDto::class.java,
        )
            .also { dto -> dto.assertFormuer(okonomi.formuer) }
    }

    @Test
    fun `Hente verdi med beskrivelse skal eksistere i Dto`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val okonomi =
            Formue(
                type = FormueType.VERDI_ANNET,
                beskrivelse = "Beskrivelse av Verdi",
            )
                .let { formue -> opprettOkonomi(soknad.id).copy(formuer = setOf(formue)) }
                .let { okonomi -> okonomiRepository.save(okonomi) }

        val verdierDto =
            doGet(
                uri = getVerdiUrl(soknad.id),
                VerdierDto::class.java,
            )
                .also { dto -> dto.assertFormuer(okonomi.formuer) }

        okonomi.formuer.find { it.type == FormueType.VERDI_ANNET }
            ?.let { formue ->
                assertThat(verdierDto.beskrivelseVerdi).isNotNull().isEqualTo(formue.beskrivelse)
            }
    }

    @Test
    fun `Oppdatere formuer skal generere okonomi-elementer og dokumentasjon`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val input =
            FormueInput(
                hasBrukskonto = true,
                hasBeskrivelseSparing = true,
                beskrivelseSparing = "Beskrivelse av Sparing",
            )
                .also { input ->
                    doPut(
                        uri = getFormueUrl(soknad.id),
                        requestBody = input,
                        responseBodyClass = input.javaClass,
                        soknadId = soknad.id,
                    )
                }

        okonomiRepository.findByIdOrNull(soknad.id)!!.let { okonomi ->
            assertThat(okonomi.formuer.toList()).hasSize(2)
                .anyMatch { it.type == FormueType.FORMUE_BRUKSKONTO }
                .anyMatch { it.type == FormueType.FORMUE_ANNET && it.beskrivelse == input.beskrivelseSparing }
        }

        dokumentasjonsRepository.findAllBySoknadId(soknad.id).let { doklist ->
            assertThat(doklist).hasSize(2)
                .anyMatch { it.type == FormueType.FORMUE_BRUKSKONTO }
                .anyMatch { it.type == FormueType.FORMUE_ANNET }
        }
    }

    @Test
    fun `Oppdatere verdier skal generere okonomi-elementer, men ikke dokumentasjon`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val input =
            HarVerdierInput(
                hasBolig = true,
                hasBeskrivelseVerdi = true,
                beskrivelseVerdi = "Beskrivelse av Verdi",
            )

        doPut(
            uri = getVerdiUrl(soknad.id),
            requestBody = input,
            responseBodyClass = VerdierDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.formuer.also { formuer ->
            assertThat(formuer.toList()).hasSize(2)
                .anyMatch { it.type == FormueType.VERDI_BOLIG }
                .anyMatch { it.type == FormueType.VERDI_ANNET && it.beskrivelse == input.beskrivelseVerdi }
        }

        assertThat(dokumentasjonsRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    @Test
    fun `Sette bekreftelse false skal fjerne alle eksisterende verdi-elementer`() {
        val soknad = soknadRepository.save(opprettSoknad())

        setOf(
            Formue(type = FormueType.VERDI_BOLIG),
            Formue(type = FormueType.VERDI_ANNET, beskrivelse = "Beskrivelse av Verdi"),
        )
            .also { okonomiRepository.save(opprettOkonomi(soknad.id).copy(formuer = it)) }

        doGet(uri = getVerdiUrl(soknad.id), responseBodyClass = VerdierDto::class.java)
            .also { dto ->
                assertThat(dto.hasBolig).isTrue()
                assertThat(dto.beskrivelseVerdi).isNotNull().isNotEmpty()
            }

        doPut(
            uri = getVerdiUrl(soknad.id),
            requestBody = HarIkkeVerdierInput(),
            responseBodyClass = VerdierDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.also {
            assertThat(it.formuer).isEmpty()
        }
    }

    @Test
    fun `Oppdatere Formue skal generere dokumentasjon, og fjerne skal slette dokumentasjon`() {
        val soknad = soknadRepository.save(opprettSoknad())

        doPut(
            uri = getFormueUrl(soknad.id),
            requestBody = FormueInput(hasBrukskonto = true),
            responseBodyClass = FormueDto::class.java,
            soknad.id,
        )
        assertThat(dokumentasjonsRepository.findAllBySoknadId(soknad.id))
            .hasSize(1).allMatch { it.type == FormueType.FORMUE_BRUKSKONTO }

        doPut(
            uri = getFormueUrl(soknad.id),
            requestBody = FormueInput(hasBrukskonto = false),
            responseBodyClass = FormueDto::class.java,
            soknad.id,
        )
        assertThat(dokumentasjonsRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    private fun FormueDto.assertFormuer(formuer: Set<Formue>) {
        FormueType.FORMUE_BRUKSKONTO.assertType(hasBrukskonto, formuer)
        FormueType.FORMUE_SPAREKONTO.assertType(hasSparekonto, formuer)
        FormueType.FORMUE_BSU.assertType(hasBsu, formuer)
        FormueType.FORMUE_LIVSFORSIKRING.assertType(hasLivsforsikring, formuer)
        FormueType.FORMUE_VERDIPAPIRER.assertType(hasVerdipapirer, formuer)
        FormueType.FORMUE_ANNET.assertType(hasSparing, formuer)
    }

    private fun VerdierDto.assertFormuer(formuer: Set<Formue>) {
        FormueType.VERDI_BOLIG.assertType(hasBolig, formuer)
        FormueType.VERDI_FRITIDSEIENDOM.assertType(hasFritidseiendom, formuer)
        FormueType.VERDI_KJORETOY.assertType(hasKjoretoy, formuer)
        FormueType.VERDI_CAMPINGVOGN.assertType(hasCampingvogn, formuer)
        FormueType.VERDI_ANNET.assertType(hasAnnetVerdi, formuer)
    }

    private fun FormueType.assertType(
        has: Boolean,
        formuer: Set<Formue>,
    ) {
        if (has) {
            assertThat(formuer).anyMatch { it.type == this }
        } else {
            assertThat(formuer).noneMatch { it.type == this }
        }
    }

    companion object {
        private fun getFormueUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/formue"

        private fun getVerdiUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/verdier"
    }
}
