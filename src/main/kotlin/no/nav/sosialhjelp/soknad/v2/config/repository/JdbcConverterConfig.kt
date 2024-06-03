package no.nav.sosialhjelp.soknad.v2.config.repository

import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseToJsonConverter
import no.nav.sosialhjelp.soknad.v2.kontakt.JsonToAdresseConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRad
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRaderToStringConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiTypeToStringConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOkonomiRadConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOkonomiTypeConverter
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
            OkonomiRaderToStringConverter<OkonomiRad>(),
            StringToOkonomiRadConverter<OkonomiRad>(),
        )
    }
}
