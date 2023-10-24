package no.nav.sosialhjelp.soknad.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseForSoknad
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseForSoknadId
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseType
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseValg
import no.nav.sosialhjelp.soknad.domene.personalia.GateAdresseObject
import no.nav.sosialhjelp.soknad.domene.personalia.MatrikkelAdresseObject
import no.nav.sosialhjelp.soknad.domene.personalia.PostboksAdresseObject
import no.nav.sosialhjelp.soknad.domene.personalia.UstrukturertAdresseObject
import no.nav.sosialhjelp.soknad.domene.personalia.repository.AdresseRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import java.util.*

class AdresseRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var adresseRepository: AdresseRepository

    @Test
    fun `Lagre og oppdater PostboksAdresse-objekt`() {
        val soknad = opprettSoknad()
        val adresseForSoknad = opprettPostboksAdresse(soknad.id)

        val lagretAdresse = adresseRepository.save(adresseForSoknad)
        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)

        val oppdatertAdresse = lagretAdresse
            .apply {
                adresse = PostboksAdresseObject(
                    postboks = "123",
                    postnummer = "123",
                    poststed = "poststed"
                )
            }
        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
    }

    @Test
    fun `Lagre og oppdater MatrikkelAdresse-objekt`() {
        val soknad = opprettSoknad()
        val adresseForSoknad = opprettMatrikkelAdresse(soknad.id)

        val lagretAdresse = adresseRepository.save(adresseForSoknad)
        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)

        val oppdatertAdresse = lagretAdresse
            .apply {
                adresse = MatrikkelAdresseObject (
                    kommunenummer = "123",
                    gaardsnummer = "123",
                    bruksnummer = "poststed"
                )
            }
        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
    }

    @Test
    fun `Lagre og oppdater GateAdresse-objekt`() {
        val soknad = opprettSoknad()
        val adresseForSoknad = opprettGateAdresse(soknad.id)

        val lagretAdresse = adresseRepository.save(adresseForSoknad)
        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)

        val oppdatertAdresse = lagretAdresse
            .apply {
                adresse = GateAdresseObject (
                    kommunenummer = "123",
                    gatenavn = "klokke",
                    husnummer = "14",
                    landkode = "NO",
                    postnummer = "2830",
                    poststed = "Raufoss"
                )
            }
        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
    }

    @Test
    fun `Lagre og oppdater UstrukturertAdresse-objekt`() {
        val soknad = opprettSoknad()
        val adresseForSoknad = opprettUstrukturertAdresse(soknad.id)

        val lagretAdresse = adresseRepository.save(adresseForSoknad)
        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)

        val oppdatertAdresse = lagretAdresse
            .apply {
                adresse = UstrukturertAdresseObject (
                    listOf(
                        "Klokkeveien 5",
                        "2830 Raufoss"
                    )
                )
            }
        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
    }

    @Test
    fun `Slett adresse`() {
        val soknad = opprettSoknad()
        val ustrukturertAdresse = opprettUstrukturertAdresse(soknad.id)
        adresseRepository.save(ustrukturertAdresse)

        assertThat(adresseRepository.existsById(ustrukturertAdresse.id)).isTrue()

        // slettemetode 1
        adresseRepository.delete(ustrukturertAdresse)
        assertThat(adresseRepository.existsById(ustrukturertAdresse.id)).isFalse()

        val ustrukturertAdresse2 = opprettUstrukturertAdresse(soknad.id)
        adresseRepository.save(ustrukturertAdresse2)
        assertThat(adresseRepository.existsById(ustrukturertAdresse2.id)).isTrue()

        // slettemetode 2
        adresseRepository.delete(ustrukturertAdresse2.id)
        assertThat(adresseRepository.existsById(ustrukturertAdresse2.id)).isFalse

        // slettemetode 3
        val ustrukturertAdresse3 = opprettUstrukturertAdresse(soknad.id)
        adresseRepository.save(ustrukturertAdresse3)
        assertThat(adresseRepository.existsById(ustrukturertAdresse3.id)).isTrue()

        adresseRepository.deleteBySoknadId(soknad.id)
        assertThat(adresseRepository.existsById(ustrukturertAdresse3.id)).isFalse()
    }

    @Test
    fun `Finner alle for SoknadId`() {
        val soknad = opprettSoknad()
        val adresseFolkeregistrert = opprettMatrikkelAdresse(soknad.id).also { adresseRepository.save(it) }

        AdresseForSoknad(
            id = AdresseForSoknadId(soknadId = soknad.id, typeAdressevalg = AdresseValg.OPPHOLD),
            adresseType = AdresseType.MATRIKKELADRESSE,
            adresse = adresseFolkeregistrert.adresse
        ).also { adresseRepository.save(it) }

        AdresseForSoknad(
            id = AdresseForSoknadId(soknadId = soknad.id, typeAdressevalg = AdresseValg.SOKNAD),
            adresseType = AdresseType.MATRIKKELADRESSE,
            adresse = adresseFolkeregistrert.adresse
        ).also { adresseRepository.save(it) }

        adresseRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it.size).isEqualTo(3)
        }
        adresseRepository.deleteBySoknadId(soknad.id)
        adresseRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it.size).isEqualTo(0)
        }
    }

    @Test
    fun `Sletter soknad sletter alle adresser`() {
        val soknad = opprettSoknad()
        val adresse = opprettMatrikkelAdresse(soknadId = soknad.id)
        adresseRepository.save(adresse)

        assertThat(adresseRepository.existsById(adresse.id)).isTrue()

        soknadRepository.delete(soknad)
        assertThat(adresseRepository.existsById(adresse.id)).isFalse()
    }

    @Test
    fun `Sammensatt primary key forsikrer begrenset antall rader`() {
        val soknad = opprettSoknad()
        val adresse = opprettPostboksAdresse(soknadId = soknad.id).also { adresseRepository.save(it) }

        assertThat(adresseRepository.existsById(adresse.id)).isTrue()

        assertThatThrownBy {
            jdbcTemplate.update(
                "INSERT INTO adresse_for_soknad (soknad_id, type_adressevalg, adresse_type, adresse_json) " +
                        "VALUES (?, ?, ?, ?)",
                adresse.id.soknadId,
                adresse.id.typeAdressevalg.name,
                adresse.adresseType.name,
                jacksonObjectMapper().writeValueAsString(adresse.adresse)
            )
        }.isInstanceOf(DuplicateKeyException::class.java)
    }

    private fun opprettPostboksAdresse(soknadId: UUID): AdresseForSoknad {
        return AdresseForSoknad(
            AdresseForSoknadId(
                soknadId = soknadId,
                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
            ),
            adresseType = AdresseType.POSTBOKSADRESSE,
            adresse = PostboksAdresseObject(
                postboks = "412",
                postnummer = "2730",
                poststed = "Lunner"
            )
        )
    }

    private fun opprettGateAdresse(soknadId: UUID): AdresseForSoknad {
        return AdresseForSoknad(
            AdresseForSoknadId(
                soknadId = soknadId,
                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
            ),
            adresseType = AdresseType.GATEADRESSE,
            adresse = GateAdresseObject(
                landkode = "412",
                kommunenummer = "2730",
                poststed = "Lunner",
                gatenavn = "Klokkeveien",
                husnummer = "14",
                postnummer = "2830"
            )
        )
    }

    private fun opprettMatrikkelAdresse(soknadId: UUID): AdresseForSoknad {
        return AdresseForSoknad(
            AdresseForSoknadId(
                soknadId = soknadId,
                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
            ),
            adresseType = AdresseType.MATRIKKELADRESSE,
            adresse = MatrikkelAdresseObject(
                kommunenummer = "412",
                gaardsnummer = "2730",
                bruksnummer = "Lunner"
            )
        )
    }

    private fun opprettUstrukturertAdresse(soknadId: UUID): AdresseForSoknad {
        return AdresseForSoknad(
            AdresseForSoknadId(
                soknadId = soknadId,
                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
            ),
            adresseType = AdresseType.USTRUKTURERT,
            adresse = UstrukturertAdresseObject(
                listOf(
                    "412",
                    "2730",
                    "Lunner"
                )
            )
        )
    }
}
