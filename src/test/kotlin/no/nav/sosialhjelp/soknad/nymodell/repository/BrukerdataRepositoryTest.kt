package no.nav.sosialhjelp.soknad.nymodell.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BegrunnelseKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.SamtykkeType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.util.*

class BrukerdataRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var brukerdataRepository: BrukerdataRepository

    @Test
    fun `Lagre brukerdata-object`() {
        val soknad = opprettSoknad()

        Brukerdata(
            soknadId = soknad.id,
            valgtAdresse = AdresseValg.SOKNAD,
            oppholdsadresse = MatrikkelAdresseObject(kommunenummer = "3211"),
        ).apply {
            samtykker[SamtykkeType.BOSTOTTE] = Samtykke(true, LocalDate.now())
            keyValueStore.update(BegrunnelseKey.HVA_SOKES_OM, "Penger!")
            keyValueStore.update(BegrunnelseKey.HVORFOR_SOKE, "Fordi jeg må")
        }.also { brukerdataRepository.save(it) }

        val findAll = brukerdataRepository.findAll()
        assertThat(findAll).hasSize(1)
        findAll.first().let {
            assertThat(it.keyValueStoreSet).hasSize(2)
        }
    }

    @Test
    fun `Lagre Brukerdata uten eksisterende Soknad skal feile`() {
        assertThatThrownBy {
            Brukerdata(soknadId = UUID.randomUUID())
                .apply { keyValueStore.update(GenerelleDataKey.KONTONUMMER, "3231.44.23133") }
                .also { brukerdataRepository.save(it) }
        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `Oppdatere Brukerdata`() {
        val soknad = opprettSoknad()

        val barneutgifter = BeskrivelseAvAnnetKey.BARNEUTGIFTER

        val brukerdata = Brukerdata(soknadId = soknad.id)
            .apply{ keyValueStore.update(barneutgifter, "Masse") }
            .also { brukerdataRepository.save(it) }

        val lagretBrukerdata = brukerdataRepository.findById(soknad.id).get()
        assertThat(lagretBrukerdata.keyValueStoreMap[barneutgifter])
            .isEqualTo(brukerdata.keyValueStoreMap[barneutgifter])

        lagretBrukerdata
            .apply { keyValueStore.update(barneutgifter, "Enda mer!") }
            .also { brukerdataRepository.save(it) }

        val oppdaterteBrukerdata = brukerdataRepository.findById(soknad.id).get()
        assertThat(oppdaterteBrukerdata.keyValueStoreMap[barneutgifter])
            .isEqualTo(lagretBrukerdata.keyValueStoreMap[barneutgifter])
    }

    @Test
    fun `Slette soknad sletter kun tilknyttede Brukerdata`() {
        val soknad1 = opprettSoknad()
        Brukerdata(soknad1.id)
            .apply { keyValueStore.update(GenerelleDataKey.KONTONUMMER, "32133323132") }
            .also { brukerdataRepository.save(it) }

        val soknad2 = opprettSoknad()
        Brukerdata(soknad2.id)
            .apply { keyValueStore.update(GenerelleDataKey.KONTONUMMER, "3213.33.23132") }
            .also { brukerdataRepository.save(it) }

        assertThat(brukerdataRepository.findAll()).hasSize(2)
        soknadRepository.delete(soknad1)
        assertThat(brukerdataRepository.findAll()).hasSize(1)

        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRUKERDATA_KEY_VALUE", Int::class.java)
            ?.let { assertThat(it).isEqualTo(1) }
    }

    @Test
    fun `Slette Brukerdata sletter kun seg selv`() {
        val soknad1 = opprettSoknad()
        Brukerdata(soknad1.id)
            .apply { keyValueStore.update(GenerelleDataKey.KONTONUMMER, "32133323132") }
            .also { brukerdataRepository.save(it) }

        val soknad2 = opprettSoknad()
        val brukerdata = Brukerdata(soknad2.id)
            .apply { keyValueStore.update(GenerelleDataKey.KONTONUMMER, "3213.33.23132") }
            .also { brukerdataRepository.save(it) }

        assertThat(brukerdataRepository.findAll()).hasSize(2)
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRUKERDATA_KEY_VALUE", Int::class.java)
            ?.let { assertThat(it).isEqualTo(2) }

        brukerdataRepository.deleteById(brukerdata.soknadId)

        assertThat(brukerdataRepository.findAll()).hasSize(1)
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRUKERDATA_KEY_VALUE", Int::class.java)
            ?.let { assertThat(it).isEqualTo(1) }
    }
}
