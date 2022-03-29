package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.db.SQLUtils
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("(dev|mock-alt|test)")
@Configuration
open class MockAltTestDbConfig {

    init {
        System.setProperty(SQLUtils.DIALECT_PROPERTY, "hsqldb")
    }
}
