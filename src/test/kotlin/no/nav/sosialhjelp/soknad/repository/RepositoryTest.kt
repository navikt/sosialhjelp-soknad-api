package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.domene.soknad.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.util.*

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // bruk datasource fra profil
@ActiveProfiles(profiles = ["test"])
abstract class RepositoryTest {

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    protected lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    companion object {
        const val EIER = "12345678901"
        const val EIER2 = "22222222222"
        const val TYPE = "bostotte|annetboutgift"
        const val TYPE2 = "dokumentasjon|aksjer"
        const val SOKNADID = 1L
        const val SOKNADID2 = 2L
        const val SOKNADID3 = 3L
        const val FILNAVN = "dokumentasjon.pdf"
    }

    fun opprettSoknad() = soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = EIER))
}
