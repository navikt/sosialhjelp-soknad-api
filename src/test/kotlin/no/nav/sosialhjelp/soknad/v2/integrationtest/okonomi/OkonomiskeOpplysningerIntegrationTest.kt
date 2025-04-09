package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenter
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.BelopDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BoliglanInput
import no.nav.sosialhjelp.soknad.v2.okonomi.BruttoNetto
import no.nav.sosialhjelp.soknad.v2.okonomi.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.GenericOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInntektDto
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.UUID

class OkonomiskeOpplysningerIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Test
    fun `Hente okonomiske opplysninger skal returnere eksisterende data`() {
        Formue(
            type = FormueType.FORMUE_BRUKSKONTO,
            formueDetaljer =
                OkonomiDetaljer(
                    listOf(Belop(belop = 2400.0)),
                ),
        )
            .also { formue -> okonomiService.addElementToOkonomi(soknad.id, formue) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
        )
            .also { dto ->
                assertThat(dto.forventetDokumentasjon).hasSize(1)
                assertThat(dto.forventetDokumentasjon.map { it.type }).containsOnly(FormueType.FORMUE_BRUKSKONTO)
            }
    }

    @Test
    fun `FormueType uten forventet dokumentasjon genererer ikke forventet dokumentasjon`() {
        Formue(
            type = FormueType.VERDI_BOLIG,
            formueDetaljer =
                OkonomiDetaljer(
                    listOf(Belop(belop = 2400.0)),
                ),
        )
            .let { formue -> okonomi.copy(formuer = setOf(formue)) }
            .let { okonomi -> okonomiRepository.save(okonomi) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).isEmpty() }
    }

    @Test
    fun `Oppdatere data skal lagres i databasen`() {
        Formue(type = FormueType.FORMUE_BRUKSKONTO)
            .also { formue -> okonomiService.addElementToOkonomi(soknad.id, formue) }

        okonomiRepository.findByIdOrNull(soknad.id)!!
            .also {
                assertThat(it.formuer).hasSize(1)
                assertThat(it.formuer.first().formueDetaljer.detaljer).isEmpty()
            }

        doPutInputAndReturnDto(
            input =
                GenericOkonomiInput(
                    okonomiOpplysningType = FormueType.FORMUE_BRUKSKONTO,
                    dokumentasjonLevert = true,
                    detaljer =
                        listOf(
                            BelopDto(belop = 2400.0),
                            BelopDto(belop = 3000.0),
                            BelopDto(belop = 1800.0),
                        ),
                ),
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!
            .let {
                assertThat(it.formuer).hasSize(1)
                it.formuer.first()
            }
            .let { formue ->
                assertThat(formue.formueDetaljer.detaljer).hasSize(3)
                assertThat(formue.formueDetaljer.detaljer).allMatch { it is Belop }
            }
    }

    @Test
    fun `Oppdatere OpplysningType som ikke finnes skal gi feil`() {
        val input =
            GenericOkonomiInput(
                okonomiOpplysningType = FormueType.FORMUE_BRUKSKONTO,
                dokumentasjonLevert = true,
                detaljer =
                    listOf(
                        BelopDto(belop = 2400.0),
                        BelopDto(belop = 3000.0),
                        BelopDto(belop = 1800.0),
                    ),
            )

        doPutExpectError(
            uri = getUrl(soknad.id),
            requestBody = input,
            httpStatus = HttpStatus.NOT_FOUND,
            soknadId = soknad.id,
        )
            .also { feilmelding ->
                assertThat(feilmelding.id).isEqualTo(soknad.id.toString())
            }
    }

    @Test
    fun `Andre utgifter med beskrivelse skal lagres pr okonomiske detalj`() {
        val enAnnenUtgiftString = "en annen utgift"
        val enTredjeUtgiftString = "en tredje utgift"

        doPutInputAndReturnDto(
            input =
                GenericOkonomiInput(
                    okonomiOpplysningType = UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
                    dokumentasjonLevert = true,
                    detaljer =
                        listOf(
                            BelopDto(beskrivelse = enAnnenUtgiftString, belop = ettBelop),
                            BelopDto(beskrivelse = enTredjeUtgiftString, belop = annetBelop),
                        ),
                ),
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).hasSize(1) }
            .forventetDokumentasjon.first().also { dokDto ->
                assertThat(dokDto.type).isEqualTo(UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
                assertThat(dokDto.dokumentasjonStatus).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
                assertThat(dokDto.detaljer)
                    .hasSize(2)
                    .anyMatch {
                        (it as BelopDto).beskrivelse == enAnnenUtgiftString && it.belop == ettBelop
                    }
                    .anyMatch {
                        (it as BelopDto).beskrivelse == enTredjeUtgiftString && it.belop == annetBelop
                    }
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.utgifter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }
            }
            .first().also { utgift ->
                assertThat(utgift.utgiftDetaljer.detaljer)
                    .anyMatch { (it as Belop).beskrivelse == enAnnenUtgiftString && it.belop == ettBelop }
                    .anyMatch { (it as Belop).beskrivelse == enTredjeUtgiftString && it.belop == annetBelop }
            }

        dokRepository.findAllBySoknadId(soknad.id).let { dokList ->
            assertThat(dokList).hasSize(1)
            assertThat(dokList.first().status).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
            assertThat(dokList.first().type).isEqualTo(UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
        }
    }

    @Test
    fun `Andre boutgifter med beskrivelse skal lagres pr okonomiske detalj`() {
        Utgift(type = UtgiftType.UTGIFTER_ANNET_BO).also {
            okonomiService.addElementToOkonomi(soknad.id, it)
        }

        val enBoutgiftString = "en annen boutgift"
        val endaEnboutgiftString = "enda en boutgift"

        doPutInputAndReturnDto(
            input =
                GenericOkonomiInput(
                    okonomiOpplysningType = UtgiftType.UTGIFTER_ANNET_BO,
                    dokumentasjonLevert = true,
                    detaljer =
                        listOf(
                            BelopDto(beskrivelse = enBoutgiftString, belop = ettBelop),
                            BelopDto(beskrivelse = endaEnboutgiftString, belop = annetBelop),
                        ),
                ),
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).hasSize(1) }
            .forventetDokumentasjon.first().also { dokDto ->
                assertThat(dokDto.dokumentasjonStatus).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
                assertThat(dokDto.type).isEqualTo(UtgiftType.UTGIFTER_ANNET_BO)
                assertThat(dokDto.detaljer)
                    .hasSize(2)
                    .anyMatch { (it as BelopDto).beskrivelse == enBoutgiftString }
                    .anyMatch { (it as BelopDto).beskrivelse == endaEnboutgiftString }
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.utgifter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == UtgiftType.UTGIFTER_ANNET_BO }
            }
            .first().also { utgift ->
                assertThat(utgift.utgiftDetaljer.detaljer)
                    .anyMatch { (it as Belop).beskrivelse == enBoutgiftString && it.belop == ettBelop }
                    .anyMatch { (it as Belop).beskrivelse == endaEnboutgiftString && it.belop == annetBelop }
            }

        dokRepository.findAllBySoknadId(soknad.id).let { dokList ->
            assertThat(dokList).hasSize(1)
            assertThat(dokList.first().status).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
            assertThat(dokList.first().type).isEqualTo(UtgiftType.UTGIFTER_ANNET_BO)
        }
    }

    @Test
    fun `Andre barneutgifter med beskrivelse skal lagres pr okonomiske detalj`() {
        Utgift(type = UtgiftType.UTGIFTER_ANNET_BARN).also {
            okonomiService.addElementToOkonomi(soknad.id, it)
        }

        val enBarneugiftString = "en annen barneutgift"
        val endaEnBarneutgiftString = "enda en barnautgift"

        doPutInputAndReturnDto(
            input =
                GenericOkonomiInput(
                    okonomiOpplysningType = UtgiftType.UTGIFTER_ANNET_BARN,
                    dokumentasjonLevert = true,
                    detaljer =
                        listOf(
                            BelopDto(beskrivelse = enBarneugiftString, belop = ettBelop),
                            BelopDto(beskrivelse = endaEnBarneutgiftString, belop = annetBelop),
                        ),
                ),
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).hasSize(1) }
            .forventetDokumentasjon.first().also { dokDto ->
                assertThat(dokDto.dokumentasjonStatus).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
                assertThat(dokDto.type).isEqualTo(UtgiftType.UTGIFTER_ANNET_BARN)
                assertThat(dokDto.detaljer)
                    .hasSize(2)
                    .anyMatch { (it as BelopDto).beskrivelse == enBarneugiftString }
                    .anyMatch { (it as BelopDto).beskrivelse == endaEnBarneutgiftString }
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.utgifter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == UtgiftType.UTGIFTER_ANNET_BARN }
            }
            .first().also { utgift ->
                assertThat(utgift.utgiftDetaljer.detaljer)
                    .anyMatch { (it as Belop).beskrivelse == enBarneugiftString && it.belop == ettBelop }
                    .anyMatch { (it as Belop).beskrivelse == endaEnBarneutgiftString && it.belop == annetBelop }
            }

        dokRepository.findAllBySoknadId(soknad.id).let { dokList ->
            assertThat(dokList).hasSize(1)
            assertThat(dokList.first().status).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
            assertThat(dokList.first().type).isEqualTo(UtgiftType.UTGIFTER_ANNET_BARN)
        }
    }

    @Test
    fun `Lonnsinntekter skal lagres i db`() {
        Inntekt(type = InntektType.JOBB).also { okonomiService.addElementToOkonomi(soknad.id, it) }

        doPutInputAndReturnDto(
            input =
                LonnsInput(
                    dokumentasjonLevert = true,
                    detalj = LonnsInntektDto(brutto = ettBelop, netto = annetBelop),
                ),
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).hasSize(1) }
            .forventetDokumentasjon.first().also { dokDto ->
                assertThat(dokDto.dokumentasjonStatus).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
                assertThat(dokDto.type).isEqualTo(InntektType.JOBB)
                assertThat(dokDto.detaljer)
                    .hasSize(1)
                    .anyMatch { (it as LonnsInntektDto).brutto == ettBelop }
                    .anyMatch { (it as LonnsInntektDto).netto == annetBelop }
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == InntektType.JOBB }
            }
            .first().also { inntekt ->
                assertThat(inntekt.inntektDetaljer.detaljer)
                    .anyMatch { (it as BruttoNetto).brutto == ettBelop }
                    .anyMatch { (it as BruttoNetto).netto == annetBelop }
            }

        dokRepository.findAllBySoknadId(soknad.id).let { dokList ->
            assertThat(dokList).hasSize(1)
            assertThat(dokList.first().status).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
            assertThat(dokList.first().type).isEqualTo(InntektType.JOBB)
        }
    }

    @Test
    fun `Boliglan skal lagres i db`() {
        Utgift(type = UtgiftType.UTGIFTER_BOLIGLAN).also { okonomiService.addElementToOkonomi(soknad.id, it) }

        doPutInputAndReturnDto(
            input =
                BoliglanInput(
                    dokumentasjonLevert = true,
                    detaljer = listOf(AvdragRenterDto(avdrag = ettBelop, renter = annetBelop)),
                ),
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).hasSize(1) }
            .forventetDokumentasjon.first().also { dokDto ->
                assertThat(dokDto.dokumentasjonStatus).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
                assertThat(dokDto.type).isEqualTo(UtgiftType.UTGIFTER_BOLIGLAN)
                assertThat(dokDto.detaljer)
                    .hasSize(1)
                    .anyMatch { (it as AvdragRenterDto).avdrag == ettBelop }
                    .anyMatch { (it as AvdragRenterDto).renter == annetBelop }
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.utgifter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == UtgiftType.UTGIFTER_BOLIGLAN }
            }
            .first().also { utgift ->
                assertThat(utgift.utgiftDetaljer.detaljer)
                    .anyMatch { (it as AvdragRenter).avdrag == ettBelop }
                    .anyMatch { (it as AvdragRenter).renter == annetBelop }
            }

        dokRepository.findAllBySoknadId(soknad.id).let { dokList ->
            assertThat(dokList).hasSize(1)
            assertThat(dokList.first().status).isEqualTo(DokumentasjonStatus.LEVERT_TIDLIGERE)
            assertThat(dokList.first().type).isEqualTo(UtgiftType.UTGIFTER_BOLIGLAN)
        }
    }

    @Test
    fun `Bostotte hentet fra register skal ikke returneres`() {
    }

    @Test
    fun `Informasjon om bostotte fra bruker skal vises`() {
    }

    companion object {
        fun getUrl(soknadId: UUID) = "/soknad/$soknadId/okonomiskeOpplysninger"

        val ettBelop = 3333.0
        val annetBelop = 4444.4
    }
}
