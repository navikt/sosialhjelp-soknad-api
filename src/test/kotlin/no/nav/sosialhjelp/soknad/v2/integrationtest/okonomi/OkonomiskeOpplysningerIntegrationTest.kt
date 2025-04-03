package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType.HUSLEIEKONTRAKT
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType.SKATTEMELDING
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.toDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.toValue
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenter
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.BelopDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BoliglanInput
import no.nav.sosialhjelp.soknad.v2.okonomi.BruttoNetto
import no.nav.sosialhjelp.soknad.v2.okonomi.GenericOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInntektDto
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeOpplysningerDto
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType.FORMUE_BRUKSKONTO
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType.VERDI_BOLIG
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType.JOBB
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType.UTGIFTER_BOLIGLAN
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType.UTGIFTER_BOLIGLAN_RENTER
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
            type = FORMUE_BRUKSKONTO,
            formueDetaljer =
                OkonomiDetaljer(
                    listOf(Belop(belop = 2400.0)),
                ),
        )
            .also { formue -> okonomiService.addElementToOkonomi(soknad.id, formue) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = OkonomiskeOpplysningerDto::class.java,
        )
            .also { dto ->
                assertThat(dto.opplysninger).hasSize(1)
                assertThat(dto.opplysninger.map { it.type.toValue() }).containsOnly(FORMUE_BRUKSKONTO)
            }

        okonomiService.getFormuer(soknad.id).filter { it.type == FORMUE_BRUKSKONTO }
            .also { formuer -> assertThat(formuer).hasSize(1) }
    }

    @Test
    fun `Hente okonomiske opplysninger skal kun returnere relevante data`() {
        val relevantForOkonomiskeOpplysninger = listOf(FORMUE_BRUKSKONTO, JOBB, UTGIFTER_BOLIGLAN)
        val ikkeRelevanteOkonomiTyper = listOf(VERDI_BOLIG, UTGIFTER_BOLIGLAN_RENTER)
        val andreDokTyper = listOf(HUSLEIEKONTRAKT, SKATTEMELDING)

        relevantForOkonomiskeOpplysninger.forEach { okonomiService.addElementToOkonomi(soknad.id, it) }
        ikkeRelevanteOkonomiTyper.forEach { okonomiService.addElementToOkonomi(soknad.id, it) }
        andreDokTyper.forEach { dokRepository.save(Dokumentasjon(soknadId = soknad.id, type = it)) }

        okonomiRepository.findByIdOrNull(soknad.id)!!
            .also {
                assertThat(it.formuer).hasSize(2)
                assertThat(it.inntekter).hasSize(1)
                assertThat(it.utgifter).hasSize(2)
            }
        dokRepository.findAllBySoknadId(soknad.id).also { assertThat(it).hasSize(5) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = OkonomiskeOpplysningerDto::class.java,
        )
            .also { dto ->
                assertThat(dto.opplysninger)
                    .hasSize(3)
                    .allMatch { relevantForOkonomiskeOpplysninger.contains(it.type.toValue()) }
                    .noneMatch { ikkeRelevanteOkonomiTyper.contains(it.type.toValue()) }
                    .noneMatch { andreDokTyper.contains(it.type.toValue()) }
            }
    }

    @Test
    fun `Oppdatere data skal lagres i databasen`() {
        Formue(type = FORMUE_BRUKSKONTO)
            .also { formue -> okonomiService.addElementToOkonomi(soknad.id, formue) }

        okonomiRepository.findByIdOrNull(soknad.id)!!
            .also {
                assertThat(it.formuer).hasSize(1)
                assertThat(it.formuer.first().formueDetaljer.detaljer).isEmpty()
            }

        doPutInputAndReturnDto(
            input =
                GenericOkonomiInput(
                    type = FORMUE_BRUKSKONTO.toDto(),
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
    fun `Oppdatere type uten dokumentasjon skal gi feil`() {
        val input =
            GenericOkonomiInput(
                type = FORMUE_BRUKSKONTO.toDto(),
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
    }

    @Test
    fun `Oppdatere ugyldig type skal gi feil`() {
        val input =
            GenericOkonomiInput(
                type = HUSLEIEKONTRAKT.toDto(),
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
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            soknadId = soknad.id,
        )
    }

    @Test
    fun `Andre utgifter med beskrivelse skal lagres pr okonomiske detalj`() {
        Dokumentasjon(
            soknadId = soknad.id,
            type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
        ).also { dokRepository.save(it) }

        val enAnnenUtgiftString = "en annen utgift"
        val enTredjeUtgiftString = "en tredje utgift"

        doPutInputAndReturnDto(
            input =
                GenericOkonomiInput(
                    type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER.toDto(),
                    detaljer =
                        listOf(
                            BelopDto(beskrivelse = enAnnenUtgiftString, belop = ettBelop),
                            BelopDto(beskrivelse = enTredjeUtgiftString, belop = annetBelop),
                        ),
                ),
        )
            .also { dto -> assertThat(dto.opplysninger).hasSize(1) }
            .opplysninger.first().also { okonoiskOpplysning ->
                assertThat(okonoiskOpplysning.type.toValue()).isEqualTo(UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
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
                    type = UtgiftType.UTGIFTER_ANNET_BO.toDto(),
                    detaljer =
                        listOf(
                            BelopDto(beskrivelse = enBoutgiftString, belop = ettBelop),
                            BelopDto(beskrivelse = endaEnboutgiftString, belop = annetBelop),
                        ),
                ),
        )
            .also { dto -> assertThat(dto.opplysninger).hasSize(1) }
            .opplysninger.first().also { okOpplysning ->
                assertThat(okOpplysning.type.toValue()).isEqualTo(UtgiftType.UTGIFTER_ANNET_BO)
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
                    type = UtgiftType.UTGIFTER_ANNET_BARN.toDto(),
                    detaljer =
                        listOf(
                            BelopDto(beskrivelse = enBarneugiftString, belop = ettBelop),
                            BelopDto(beskrivelse = endaEnBarneutgiftString, belop = annetBelop),
                        ),
                ),
        )
            .also { dto -> assertThat(dto.opplysninger).hasSize(1) }
            .opplysninger.first().also { okOpplysning ->
                assertThat(okOpplysning.type.toValue()).isEqualTo(UtgiftType.UTGIFTER_ANNET_BARN)
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
    }

    @Test
    fun `Lonnsinntekter skal lagres i db`() {
        Inntekt(type = JOBB).also { okonomiService.addElementToOkonomi(soknad.id, it) }

        doPutInputAndReturnDto(
            input =
                LonnsInput(
                    detalj = LonnsInntektDto(brutto = ettBelop, netto = annetBelop),
                ),
        )
            .also { dto -> assertThat(dto.opplysninger).hasSize(1) }
            .opplysninger.first().also { okOpplysning ->
                assertThat(okOpplysning.type.toValue()).isEqualTo(JOBB)
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == JOBB }
            }
            .first().also { inntekt ->
                assertThat(inntekt.inntektDetaljer.detaljer)
                    .anyMatch { (it as BruttoNetto).brutto == ettBelop }
                    .anyMatch { (it as BruttoNetto).netto == annetBelop }
            }
    }

    @Test
    fun `Boliglan skal lagres i db`() {
        Utgift(type = UTGIFTER_BOLIGLAN).also { okonomiService.addElementToOkonomi(soknad.id, it) }

        doPutInputAndReturnDto(
            input =
                BoliglanInput(
                    detaljer = listOf(AvdragRenterDto(avdrag = ettBelop, renter = annetBelop)),
                ),
        )
            .also { dto -> assertThat(dto.opplysninger).hasSize(1) }
            .opplysninger.first().also { okOpplysning ->
                assertThat(okOpplysning.type.toValue()).isEqualTo(UTGIFTER_BOLIGLAN)
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.utgifter
            .also { utgifter ->
                assertThat(utgifter.toList())
                    .hasSize(1)
                    .allMatch { it.type == UTGIFTER_BOLIGLAN }
            }
            .first().also { utgift ->
                assertThat(utgift.utgiftDetaljer.detaljer)
                    .anyMatch { (it as AvdragRenter).avdrag == ettBelop }
                    .anyMatch { (it as AvdragRenter).renter == annetBelop }
            }
    }

    @Test
    fun `Bostotte hentet fra register skal ikke returneres`() {
        // TODO
    }

    @Test
    fun `Informasjon om bostotte fra bruker skal vises`() {
        // TODO
    }

    companion object {
        fun getUrl(soknadId: UUID) = "/soknad/$soknadId/okonomiskeOpplysninger"

        val ettBelop = 3333.0
        val annetBelop = 4444.4
    }
}
