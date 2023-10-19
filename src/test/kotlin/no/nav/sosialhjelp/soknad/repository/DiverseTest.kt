package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.personalia.AdresseForSoknad
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jdbc.core.mapping.BasicJdbcPersistentProperty
import org.springframework.data.mapping.PersistentEntity
import org.springframework.data.relational.core.mapping.BasicRelationalPersistentProperty
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy
import org.springframework.data.relational.core.mapping.NamingStrategy
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty
import org.springframework.test.context.ActiveProfiles

class DiverseTest {

    @Test
    fun `Test av naming strategy`() {

    }
}