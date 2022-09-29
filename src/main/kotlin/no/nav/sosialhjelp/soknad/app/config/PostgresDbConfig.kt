package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.db.SQLUtils
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("postgres-gcp")
@Configuration
open class PostgresDbConfig {

    init {
        System.setProperty(SQLUtils.DIALECT_PROPERTY, "postgresql")
    }
}
