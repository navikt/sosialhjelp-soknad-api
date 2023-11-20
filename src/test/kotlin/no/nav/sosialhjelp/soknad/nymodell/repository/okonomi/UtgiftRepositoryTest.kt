package no.nav.sosialhjelp.soknad.nymodell.repository.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class UtgiftRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var utgiftRepository: UtgiftRepository

    @Test
    fun `Lagre ny utgift`() {
        val soknad = opprettSoknad()

        val utgift = createFullUtgift(soknad.id)

        utgiftRepository.save(utgift)

        val allUtgift = utgiftRepository.findAll()
        allUtgift.size

        val numberOfRows = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM bekreftelse", Int::class.java
        )

        numberOfRows.dec()
    }
}

fun createFullUtgift(soknadId: UUID): Utgift {
    return Utgift(
        soknadId = soknadId,
        type = UtgiftType.ANDRE_UTGIFTER,
        tittel = "Utgift tittel",
        belop = 500
    )
}
