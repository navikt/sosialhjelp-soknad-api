package no.nav.sosialhjelp.soknad.nymodell.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata.AdresseObjectToJsonConverter
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata.BrukerdataKey
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata.BrukerdataValue
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata.JsonToAdresseObjectConverter
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.repository.BrukerdataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

val mapper = jacksonObjectMapper()

@Import(MyJdbcConfiguration::class)
class BrukerdataRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var brukerdataRepository: BrukerdataRepository

//    @Autowired
//    private lateinit var ctx: ApplicationContext
//
//    @Autowired
//    private lateinit var soknadRepository: SoknadRepository

    @Test
    fun `Lagre brukerdata-object`() {
        val soknad = opprettSoknad()

        Brukerdata(
            soknadId = soknad.id,
            valgtAdresse = AdresseValg.SOKNAD,
            oppholdsadresse = MatrikkelAdresseObject(kommunenummer = "3211"),
            andreData = mutableMapOf(BrukerdataKey.KONTONUMMER to BrukerdataValue("3223.41.22313"))

        ).also { brukerdataRepository.save(it) }

        val findAll = brukerdataRepository.findAll()
        assertThat(findAll).hasSize(1)
    }

//    fun opprettSoknad() = soknadRepository.save(
//        Soknad(
//            id = UUID.randomUUID(),
//            eier = Eier(
//                personId = RepositoryTest.EIER,
//                navn = Navn(
//                    fornavn = "Fornavn",
//                    etternavn = "Etternavnsen",
//                ),
//                kontaktInfo = KontaktInfo(
//                    telefonnummer = "41231322",
//                    folkeregistrertAdresse = FolkeregistrertAdresse(
//                        adresseType = AdresseType.GATEADRESSE,
//                        adresseJson = "En adresse"
//                    ),
//                )
//            ),
//        )
//    )

//    @Test
//    fun `Lagre Brukerdata uten eksisterende Soknad skal feile`() {
//        assertThatThrownBy {
//            Brukerdata(UUID.randomUUID(), mutableMapOf(BrukerdataType.KONTONUMMER to BrukerdataValue("3213.33.23132")))
//                .also { brukerdataRepository.save(it) }
//        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
//    }

    //    @Test
//    fun `Oppdatere Brukerdata`() {
//        val soknad = opprettSoknad()
//
//        val barneutgifter = BeskrivelseAvAnnetType.BARNEUTGIFTER
//
//        val brukerdata = Brukerdata(
//            soknadId = soknad.id,
//            brukerdata = mutableMapOf(barneutgifter to BrukerdataValue("Masse barneutgifter")),
//        ).also { brukerdataRepository.save(it) }
//
//        val lagretBrukerdata = brukerdataRepository.findAll().first() ?: throw IllegalStateException("Ikke lagret")
//        val brukerdataValue = lagretBrukerdata.brukerdata[barneutgifter] ?: throw IllegalStateException("Ikke lagret")
//
//
//
//        val oppdatertBrukerdata = Brukerdata(
//            soknadId = soknad.id,
//            brukerdata = mutableMapOf(BeskrivelseAvAnnetType.BARNEUTGIFTER to BrukerdataValue("Enda mer"))
//        ).also { brukerdataRepository.save(it) }
//
//        brukerdataRepository.findAll().first().let {
//            assertThat(it.brukerdata[BeskrivelseAvAnnetType.BARNEUTGIFTER]?.value)
//                .isEqualTo(it.brukerdata[BeskrivelseAvAnnetType.BARNEUTGIFTER]?.value)
//        }
//    }
//
//    @Test
//    fun `Slette soknad sletter kun tilknyttede Brukerdata`() {
//        val soknad1 = opprettSoknad()
//        Brukerdata(soknad1.id, BrukerdataType.KONTONUMMER, "32133323132")
//            .also { brukerdataRepository.save(it) }
//
//        val soknad2 = opprettSoknad()
//        Brukerdata(soknad2.id, BrukerdataType.KONTONUMMER, "3213.33.23132")
//            .also { brukerdataRepository.save(it) }
//
//        assertThat(brukerdataRepository.findAll()).hasSize(2)
//        soknadRepository.delete(soknad1)
//        assertThat(brukerdataRepository.findAll()).hasSize(1)
//    }
//
//    @Test
//    fun `Slette Brukerdata sletter kun seg selv`() {
//        val soknad1 = opprettSoknad()
//        Brukerdata(soknad1.id, BrukerdataType.KONTONUMMER, "32133323132")
//            .also { brukerdataRepository.save(it) }
//
//        val soknad2 = opprettSoknad()
//        val brukerdata = Brukerdata(soknad2.id, BrukerdataType.KONTONUMMER, "3213.33.23132")
//            .also { brukerdataRepository.save(it) }
//
//        assertThat(brukerdataRepository.findAll()).hasSize(2)
//        brukerdataRepository.delete(brukerdata.soknadId, brukerdata.key)
//        assertThat(brukerdataRepository.findAll()).hasSize(1)
//    }

}

@TestConfiguration
class MyJdbcConfiguration: AbstractJdbcConfiguration() {

    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(mutableListOf(JsonToAdresseObjectConverter(), AdresseObjectToJsonConverter()))
    }

    override fun userConverters(): MutableList<Converter<*, *>> {
        return mutableListOf(JsonToAdresseObjectConverter(), AdresseObjectToJsonConverter())
    }
}