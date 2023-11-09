//package no.nav.sosialhjelp.soknad.nymodell.repository
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.Adresse
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseId
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseType
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.GateAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.PostboksAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.UstrukturertAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.repository.AdresseRepository
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.dao.DuplicateKeyException
//import java.util.*
//
//class AdresseRepositoryTest: RepositoryTest() {
//
//    @Autowired
//    private lateinit var adresseRepository: AdresseRepository
//
//    @Test
//    fun `Lagre og oppdater PostboksAdresse-objekt`() {
//        val soknad = opprettSoknad()
//        val adresseForSoknad = opprettPostboksAdresse(soknad.soknadId)
//
//        val lagretAdresse = adresseRepository.save(adresseForSoknad)
//        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)
//
//        val oppdatertAdresse = lagretAdresse
//            .apply {
//                adresseObject = PostboksAdresseObject(
//                    postboks = "123",
//                    postnummer = "123",
//                    poststed = "poststed"
//                )
//            }
//        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
//        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
//    }
//
//    @Test
//    fun `Lagre og oppdater MatrikkelAdresse-objekt`() {
//        val soknad = opprettSoknad()
//        val adresseForSoknad = opprettMatrikkelAdresse(soknad.soknadId)
//
//        val lagretAdresse = adresseRepository.save(adresseForSoknad)
//        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)
//
//        val oppdatertAdresse = lagretAdresse
//            .apply {
//                adresseObject = MatrikkelAdresseObject (
//                    kommunenummer = "123",
//                    gaardsnummer = "123",
//                    bruksnummer = "poststed"
//                )
//            }
//        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
//        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
//    }
//
//    @Test
//    fun `Lagre og oppdater GateAdresse-objekt`() {
//        val soknad = opprettSoknad()
//        val adresseForSoknad = opprettGateAdresse(soknad.soknadId)
//
//        val lagretAdresse = adresseRepository.save(adresseForSoknad)
//        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)
//
//        val oppdatertAdresse = lagretAdresse
//            .apply {
//                adresseObject = GateAdresseObject (
//                    kommunenummer = "123",
//                    gatenavn = "klokke",
//                    husnummer = "14",
//                    landkode = "NO",
//                    postnummer = "2830",
//                    poststed = "Raufoss"
//                )
//            }
//        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
//        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
//    }
//
//    @Test
//    fun `Lagre og oppdater UstrukturertAdresse-objekt`() {
//        val soknad = opprettSoknad()
//        val adresseForSoknad = opprettUstrukturertAdresse(soknad.soknadId)
//
//        val lagretAdresse = adresseRepository.save(adresseForSoknad)
//        assertThat(adresseForSoknad).isEqualTo(lagretAdresse)
//
//        val oppdatertAdresse = lagretAdresse
//            .apply {
//                adresseObject = UstrukturertAdresseObject (
//                    listOf(
//                        "Klokkeveien 5",
//                        "2830 Raufoss"
//                    )
//                )
//            }
//        val lagretOppdatertAdresse = adresseRepository.save(oppdatertAdresse)
//        assertThat(lagretOppdatertAdresse).isEqualTo(oppdatertAdresse)
//    }
//
//    @Test
//    fun `Slett adresse`() {
//        val soknad = opprettSoknad()
//        val ustrukturertAdresse = opprettUstrukturertAdresse(soknad.soknadId)
//        adresseRepository.save(ustrukturertAdresse)
//
//        assertThat(adresseRepository.existsById(ustrukturertAdresse.id)).isTrue()
//
//        // slettemetode 1
//        adresseRepository.delete(ustrukturertAdresse)
//        assertThat(adresseRepository.existsById(ustrukturertAdresse.id)).isFalse()
//
//        val ustrukturertAdresse2 = opprettUstrukturertAdresse(soknad.soknadId)
//        adresseRepository.save(ustrukturertAdresse2)
//        assertThat(adresseRepository.existsById(ustrukturertAdresse2.id)).isTrue()
//
//        // slettemetode 2
//        adresseRepository.delete(ustrukturertAdresse2.id)
//        assertThat(adresseRepository.existsById(ustrukturertAdresse2.id)).isFalse
//
//        // slettemetode 3
//        val ustrukturertAdresse3 = opprettUstrukturertAdresse(soknad.soknadId)
//        adresseRepository.save(ustrukturertAdresse3)
//        assertThat(adresseRepository.existsById(ustrukturertAdresse3.id)).isTrue()
//
//        adresseRepository.deleteBySoknadId(soknad.soknadId)
//        assertThat(adresseRepository.existsById(ustrukturertAdresse3.id)).isFalse()
//    }
//
//    @Test
//    fun `Finner alle for SoknadId`() {
//        val soknad = opprettSoknad()
//        val adresseFolkeregistrert = opprettMatrikkelAdresse(soknad.soknadId).also { adresseRepository.save(it) }
//
//        Adresse(
//            id = AdresseId(soknadId = soknad.soknadId, typeAdressevalg = AdresseValg.SOKNAD),
//            adresseType = AdresseType.MATRIKKELADRESSE,
//            adresseObject = adresseFolkeregistrert.adresseObject
//        ).also { adresseRepository.save(it) }
//
//        Adresse(
//            id = AdresseId(soknadId = soknad.soknadId, typeAdressevalg = AdresseValg.MIDLERTIDIG),
//            adresseType = AdresseType.MATRIKKELADRESSE,
//            adresseObject = adresseFolkeregistrert.adresseObject
//        ).also { adresseRepository.save(it) }
//
//        adresseRepository.findAllBySoknadId(soknad.soknadId).also {
//            assertThat(it.size).isEqualTo(3)
//        }
//        adresseRepository.deleteBySoknadId(soknad.soknadId)
//        adresseRepository.findAllBySoknadId(soknad.soknadId).also {
//            assertThat(it.size).isEqualTo(0)
//        }
//    }
//
//    @Test
//    fun `Sletter soknad sletter alle adresser`() {
//        val soknad = opprettSoknad()
//        val adresseObject = opprettMatrikkelAdresse(soknadId = soknad.soknadId)
//        adresseRepository.save(adresseObject)
//
//        assertThat(adresseRepository.existsById(adresseObject.id)).isTrue()
//
//        soknadRepository.delete(soknad)
//        assertThat(adresseRepository.existsById(adresseObject.id)).isFalse()
//    }
//
//    @Test
//    fun `Sammensatt primary key forsikrer begrenset antall rader`() {
//        val soknad = opprettSoknad()
//        val adresseObject = opprettPostboksAdresse(soknadId = soknad.soknadId).also { adresseRepository.save(it) }
//
//        assertThat(adresseRepository.existsById(adresseObject.id)).isTrue()
//
//        assertThatThrownBy {
//            jdbcTemplate.update(
//                "INSERT INTO adresse_for_soknad (soknad_id, type_adressevalg, adresse_type, adresse_json) " +
//                        "VALUES (?, ?, ?, ?)",
//                adresseObject.id.soknadId,
//                adresseObject.id.typeAdressevalg.name,
//                adresseObject.adresseType.name,
//                jacksonObjectMapper().writeValueAsString(adresseObject)
//            )
//        }.isInstanceOf(DuplicateKeyException::class.java)
//    }
//
//    private fun opprettPostboksAdresse(soknadId: UUID): Adresse {
//        return Adresse(
//            AdresseId(
//                soknadId = soknadId,
//                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
//            ),
//            adresseType = AdresseType.POSTBOKSADRESSE,
//            adresseObject = PostboksAdresseObject(
//                postboks = "412",
//                postnummer = "2730",
//                poststed = "Lunner"
//            )
//        )
//    }
//
//    private fun opprettGateAdresse(soknadId: UUID): Adresse {
//        return Adresse(
//            AdresseId(
//                soknadId = soknadId,
//                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
//            ),
//            adresseType = AdresseType.GATEADRESSE,
//            adresseObject = GateAdresseObject(
//                landkode = "412",
//                kommunenummer = "2730",
//                poststed = "Lunner",
//                gatenavn = "Klokkeveien",
//                husnummer = "14",
//                postnummer = "2830"
//            )
//        )
//    }
//
//    private fun opprettMatrikkelAdresse(soknadId: UUID): Adresse {
//        return Adresse(
//            AdresseId(
//                soknadId = soknadId,
//                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
//            ),
//            adresseType = AdresseType.MATRIKKELADRESSE,
//            adresseObject = MatrikkelAdresseObject(
//                kommunenummer = "412",
//                gaardsnummer = "2730",
//                bruksnummer = "Lunner"
//            )
//        )
//    }
//
//    private fun opprettUstrukturertAdresse(soknadId: UUID): Adresse {
//        return Adresse(
//            AdresseId(
//                soknadId = soknadId,
//                typeAdressevalg = AdresseValg.FOLKEREGISTRERT,
//            ),
//            adresseType = AdresseType.USTRUKTURERT,
//            adresseObject = UstrukturertAdresseObject(
//                listOf(
//                    "412",
//                    "2730",
//                    "Lunner"
//                )
//            )
//        )
//    }
//}
