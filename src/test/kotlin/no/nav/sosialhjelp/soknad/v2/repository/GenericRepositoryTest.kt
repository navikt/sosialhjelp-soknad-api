package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettDokumentasjon
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettIntegrasjonstatus
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettOkonomi
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Formålet med testklassen:
 * 1. Sjekke at domenemodell samsvarer med database-tabeller
 * 2. Sjekke at "delete on cascade" i databasen fungerer som forventet
 * 3. Sjekke at fremmednøkkel er korrekt koblet til eksisterende soknad
 */
class GenericRepositoryTest : AbstractGenericRepositoryTest() {
    @Test
    fun `Verifisere CRUD-operasjoner for Soknad`() {
        // for "rot"-objektet vil det ikke være constraints som må testes
        val original = soknadRepository.save(opprettSoknad(soknad.id))
        val updated = soknadRepository.save(opprettSoknad(soknad.id).copy(eierPersonId = "NOE ANNET"))
        assertThat(original).isNotEqualTo(updated)
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Livssituasjon`() {
        livssituasjonRepository.verifyCRUDOperations(
            originalEntity = opprettLivssituasjon(soknad.id),
            updatedEntity = opprettLivssituasjon(soknad.id).copy(bosituasjon = Bosituasjon(antallHusstand = 999)),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Eier`() {
        eierRepository.verifyCRUDOperations(
            originalEntity = opprettEier(soknad.id),
            updatedEntity = opprettEier(soknad.id).copy(statsborgerskap = "SPANSK"),
        )
    }

    @Test
    fun `Hente eiers personId skal returnere eierPersonId fra Soknad`() {
        val eier = eierRepository.save(opprettEier(soknad.id))
        assertThat(eierRepository.getEierPersonId(eier.soknadId)).isEqualTo(soknad.eierPersonId)
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Kontakt`() {
        kontaktRepository.verifyCRUDOperations(
            originalEntity = opprettKontakt(soknad.id),
            updatedEntity = opprettKontakt(soknad.id).copy(telefonnummer = Telefonnummer(fraBruker = "99221199")),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Familie`() {
        familieRepository.verifyCRUDOperations(
            originalEntity = createFamilie(soknad.id),
            updatedEntity =
                createFamilie(soknad.id)
                    .copy(sivilstatus = null),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Integrasjonstatus`() {
        integrasjonstatusRepository.verifyCRUDOperations(
            originalEntity = opprettIntegrasjonstatus(soknad.id),
            updatedEntity = opprettIntegrasjonstatus(soknad.id).copy(feilUtbetalingerNav = true),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Okonomi`() {
        okonomiRepository.verifyCRUDOperations(
            originalEntity = opprettOkonomi(soknad.id),
            updatedEntity = opprettOkonomi(soknad.id).copy(utgifter = emptySet()),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Dokumentasjon`() {
        val dbId = UUID.randomUUID()
        dokumentasjonRepository.verifyCRUDOperations(
            originalEntity = opprettDokumentasjon(dbId, soknad.id),
            updatedEntity = opprettDokumentasjon(dbId, soknad.id).copy(status = DokumentasjonStatus.LASTET_OPP),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Metadata`() {
        val soknadMetadata = opprettSoknadMetadata()

        soknadMetadataRepository.save(soknadMetadata)
        soknadMetadataRepository.findByIdOrNull(soknadMetadata.soknadId)!!
            .also { assertThat(it).isEqualTo(soknadMetadata) }

        val oppdatertMetadata =
            soknadMetadata.copy(
                status = SoknadStatus.SENDT,
                tidspunkt = soknadMetadata.tidspunkt.copy(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
            )
        soknadMetadataRepository.save(oppdatertMetadata)
        soknadMetadataRepository.findByIdOrNull(oppdatertMetadata.soknadId)!!
            .also { assertThat(it).isEqualTo(oppdatertMetadata) }

        soknadMetadataRepository.deleteById(soknadMetadata.soknadId)
        soknadMetadataRepository.findByIdOrNull(soknadMetadata.soknadId)
            .also { assertThat(it).isNull() }
    }

    @Test
    fun `Insert til Inntekt selvom PK eksisterer skal fungere`() {
        okonomiRepository.save(opprettOkonomi(soknad.id))

        okonomiRepository.addInntekt(soknad.id, type = InntektType.BARNEBIDRAG_MOTTAR, null, null)

        assertDoesNotThrow {
            okonomiRepository.addInntekt(soknad.id, InntektType.BARNEBIDRAG_MOTTAR, "Beskrivelse", null)
        }

        okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.find { it.type == InntektType.BARNEBIDRAG_MOTTAR }!!
            .also { assertThat(it.beskrivelse).isEqualTo("Beskrivelse") }
    }

    @Test
    fun `Insert til Utgift selvom PK eksisterer skal fungere`() {
        okonomiRepository.save(opprettOkonomi(soknad.id))

        okonomiRepository.addUtgift(soknad.id, type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER, null, null)

        assertDoesNotThrow {
            okonomiRepository.addUtgift(soknad.id, UtgiftType.UTGIFTER_ANDRE_UTGIFTER, "Beskrivelse", null)
        }

        okonomiRepository.findByIdOrNull(soknad.id)!!.utgifter.find { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }!!
            .also { assertThat(it.beskrivelse).isEqualTo("Beskrivelse") }
    }

    @Test
    fun `Insert til Formue selvom PK eksisterer skal fungere`() {
        okonomiRepository.save(opprettOkonomi(soknad.id))

        okonomiRepository.addFormue(soknad.id, type = FormueType.FORMUE_BRUKSKONTO, null, null)

        assertDoesNotThrow {
            okonomiRepository.addFormue(soknad.id, FormueType.FORMUE_BRUKSKONTO, "Beskrivelse", null)
        }

        okonomiRepository.findByIdOrNull(soknad.id)!!.formuer.find { it.type == FormueType.FORMUE_BRUKSKONTO }!!
            .also { assertThat(it.beskrivelse).isEqualTo("Beskrivelse") }
    }

    @Test
    fun `Insert til Bekreftelse selvom PK eksisterer skal fungere`() {
        val tidspunkt = LocalDateTime.now().minusSeconds(10)

        okonomiRepository.save(opprettOkonomi(soknad.id))

        okonomiRepository.addBekreftelse(
            soknad.id,
            type = BekreftelseType.BEKREFTELSE_VERDI,
            tidspunkt = LocalDateTime.now().minusSeconds(1),
            true,
        )

        assertDoesNotThrow {
            okonomiRepository.addBekreftelse(
                soknad.id,
                type = BekreftelseType.BEKREFTELSE_VERDI,
                tidspunkt = LocalDateTime.now(),
                false,
            )
        }

        okonomiRepository.findByIdOrNull(soknad.id)!!.bekreftelser.find { it.type == BekreftelseType.BEKREFTELSE_VERDI }!!
            .also {
                assertThat(it.tidspunkt).isNotEqualTo(tidspunkt)
                assertThat(it.tidspunkt.isAfter(tidspunkt)).isTrue()
            }
    }
}
