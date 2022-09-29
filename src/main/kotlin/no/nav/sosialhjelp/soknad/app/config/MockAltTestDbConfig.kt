package no.nav.sosialhjelp.soknad.app.config

import no.nav.sosialhjelp.soknad.db.SQLUtils
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("(mock-alt|test)")
@Configuration
open class MockAltTestDbConfig {

    init {
        System.setProperty(SQLUtils.DIALECT_PROPERTY, "hsqldb")
    }
}
