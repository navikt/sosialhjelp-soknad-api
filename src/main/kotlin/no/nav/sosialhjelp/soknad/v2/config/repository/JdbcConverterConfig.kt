package no.nav.sosialhjelp.soknad.v2.config.repository

import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseToJsonConverter
import no.nav.sosialhjelp.soknad.v2.kontakt.JsonToAdresseConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiTypeToStringConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljerToStringConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOkonomiTypeConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOkonomiskeDetaljerConverter
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
            OkonomiskeDetaljerToStringConverter<OkonomiDetalj>(),
            StringToOkonomiskeDetaljerConverter<OkonomiDetalj>(),
        )
    }
}
