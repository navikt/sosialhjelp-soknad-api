package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.FamilieToJsonMapper
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator as validator

class FamilieToJsonMapperTest : AbstractMapperTest() {
    private val mapper = FamilieToJsonMapper.Mapper

    @Test
    fun `Status Gift uten ektefelle skal kaste valideringsfeil`() {
        skipAfterEach = true

        json =
            FamilieToJsonMapper.doMapping(
                Familie(
                    soknadId = UUID.randomUUID(),
                    sivilstatus = Sivilstatus.GIFT,
                ),
                json,
            )

        val soknad = requireNotNull(json.soknad)
        val familie = requireNotNull(soknad.data.familie)
        json = json.copy(soknad = soknad.copy(data = soknad.data.copy(familie = familie.copy(sivilstatus = requireNotNull(familie.sivilstatus).copy(ektefelle = null)))))

        objectMapper.writeValueAsString(json.soknad)
            .also {
                assertThatThrownBy { validator.ensureValidSoknad(it) }
                    .isInstanceOf(JsonSosialhjelpValidationException::class.java)
            }
    }

    @Test
    fun `Barn fra register skal ha harDiskresjonskode false`() {
        val barn = Barn(navn = Navn("Ola", null, "Nordmann"), folkeregistrertSammen = true)
        json = FamilieToJsonMapper.doMapping(Familie(soknadId = UUID.randomUUID(), ansvar = mapOf(UUID.randomUUID() to barn)), json)

        val jsonBarn = requireNotNull(json.soknad?.data?.familie).forsorgerplikt.ansvar.single().barn
        assertThat(requireNotNull(jsonBarn).harDiskresjonskode).isFalse()
    }

    @Test
    fun `Ektefelle uten navn skal gi tomt navn`() {
        val ektefelle = Ektefelle(navn = null, fodselsdato = null, personId = null)
        json = FamilieToJsonMapper.doMapping(Familie(soknadId = UUID.randomUUID(), sivilstatus = Sivilstatus.GIFT, ektefelle = ektefelle), json)

        val navn = requireNotNull(json.soknad?.data?.familie?.sivilstatus?.ektefelle).navn
        assertThat(listOf(navn.fornavn, navn.mellomnavn, navn.etternavn)).allMatch { it.isEmpty() }
    }

    @Test
    fun `Systemregistrert ektefelle skal kun ha folkeregisteropplysninger`() {
        val ektefelle =
            Ektefelle(
                navn = null,
                fodselsdato = null,
                personId = null,
                folkeregistrertMedEktefelle = true,
                borSammen = false,
            )
        json = FamilieToJsonMapper.doMapping(Familie(UUID.randomUUID(), sivilstatus = Sivilstatus.GIFT, ektefelle = ektefelle), json)

        val sivilstatus = requireNotNull(json.soknad?.data?.familie?.sivilstatus)
        assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(sivilstatus.ektefelleHarDiskresjonskode).isFalse()
        assertThat(sivilstatus.folkeregistrertMedEktefelle).isTrue()
        assertThat(sivilstatus.borSammenMed).isNull()
    }

    @Test
    fun `Brukerregistrert ektefelle skal kun ha brukersvar`() {
        val ektefelle =
            Ektefelle(
                navn = null,
                fodselsdato = null,
                personId = null,
                folkeregistrertMedEktefelle = true,
                borSammen = false,
                kildeErSystem = false,
            )
        json = FamilieToJsonMapper.doMapping(Familie(UUID.randomUUID(), sivilstatus = Sivilstatus.GIFT, ektefelle = ektefelle), json)

        val sivilstatus = requireNotNull(json.soknad?.data?.familie?.sivilstatus)
        assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(sivilstatus.ektefelleHarDiskresjonskode).isNull()
        assertThat(sivilstatus.folkeregistrertMedEktefelle).isNull()
        assertThat(sivilstatus.borSammenMed).isFalse()
    }

    @Test
    fun `Status gift uten ektefelle skal gi Ektefelle med tomt navn`() {
        json =
            FamilieToJsonMapper.doMapping(
                Familie(
                    soknadId = UUID.randomUUID(),
                    sivilstatus = Sivilstatus.GIFT,
                ),
                json,
            )

        with(requireNotNull(json.soknad?.data?.familie?.sivilstatus)) {
            assertThat(status).isEqualTo(JsonSivilstatus.Status.valueOf(Sivilstatus.GIFT.name))
            assertThat(ektefelle).isNotNull
            assertThat(requireNotNull(ektefelle).navn).isNotNull
            assertThat(requireNotNull(ektefelle?.navn).fornavn).isBlank()
            assertThat(requireNotNull(ektefelle?.navn).mellomnavn).isBlank()
            assertThat(requireNotNull(ektefelle?.navn).etternavn).isBlank()
        }
    }
}
