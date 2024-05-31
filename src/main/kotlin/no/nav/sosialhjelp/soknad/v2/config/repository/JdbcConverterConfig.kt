package no.nav.sosialhjelp.soknad.v2.config.repository

import no.nav.sosialhjelp.soknad.v2.config.repository.converters.AdresseToJsonConverter
import no.nav.sosialhjelp.soknad.v2.config.repository.converters.JsonToAdresseConverter
import no.nav.sosialhjelp.soknad.v2.config.repository.converters.OkonomiTypeToStringConverter
import no.nav.sosialhjelp.soknad.v2.config.repository.converters.StringToOkonomiTypeConverter
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

/**
 * For å støtte spesielle typer som skal lagres i databasen
 */
@Configuration
class JdbcConverterConfig : AbstractJdbcConfiguration() {
    override fun userConverters(): MutableList<*> {
        return mutableListOf(
            AdresseToJsonConverter,
            JsonToAdresseConverter,
            OkonomiTypeToStringConverter,
            StringToOkonomiTypeConverter,
        )
    }
}
