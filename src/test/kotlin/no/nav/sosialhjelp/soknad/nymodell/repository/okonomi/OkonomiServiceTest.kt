package no.nav.sosialhjelp.soknad.nymodell.repository.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftRepository
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class OkonomiServiceTest: RepositoryTest() {

    @Autowired
    private lateinit var inntektRepository: InntektRepository

    @Autowired
    private lateinit var utgiftRepository: UtgiftRepository

    @Test
    fun `Sjekk logikk for felles tabell`() {
        val soknad = opprettSoknad()

        val inntekt = createFullInntekt(soknad.id).also { inntektRepository.save(it) }
        createFullInntekt(soknad.id).also { inntektRepository.save(it) }

        createFullUtgift(soknad.id).also { utgiftRepository.save(it) }
        createFullUtgift(soknad.id).also { utgiftRepository.save(it) }

        checkNumberOfRows("BEKREFTELSE")
        checkNumberOfRows("UTBETALING")

        inntektRepository.delete(inntekt)

        val findAllInntekt = inntektRepository.findAll()
        val findAllUtgift = utgiftRepository.findAll()

        checkNumberOfRows("BEKREFTELSE")
        checkNumberOfRows("UTBETALING")

        soknadRepository.delete(soknad)

        val allInntekt = inntektRepository.findAll()
        val allUtgift = utgiftRepository.findAll()

        checkNumberOfRows("BEKREFTELSE")
        checkNumberOfRows("UTBETALING")
    }


    private fun checkNumberOfRows(tableName: String): Int? {
        val numberOfRows = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM $tableName", Int::class.java
        )
        return numberOfRows
    }
}
