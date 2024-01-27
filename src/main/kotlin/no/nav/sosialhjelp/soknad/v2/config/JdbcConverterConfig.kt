package no.nav.sosialhjelp.soknad.v2.config

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.v2.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.PostboksAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.VegAdresse
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@Configuration
class JdbcConverterConfig: AbstractJdbcConfiguration() {

    override fun userConverters(): MutableList<*> {
        return mutableListOf(
            AdresseToJsonConverter,
            JsonToAdresseConverter
        )
    }

    @WritingConverter
    object AdresseToJsonConverter : Converter<Adresse, String> {
        override fun convert(source: Adresse): String = mapper.writeValueAsString(source)
    }

    @ReadingConverter
    object JsonToAdresseConverter : Converter<String, Adresse> {
        override fun convert(source: String): Adresse = JsonToAdresseMapper.map(source)
    }

    private object JsonToAdresseMapper {
        val adresseTyper = setOf(
            VegAdresse::class.java,
            MatrikkelAdresse::class.java,
            PostboksAdresse::class.java,
            UstrukturertAdresse::class.java
        )

        fun map(json: String): Adresse {
            adresseTyper.forEach {
                try {
                    return mapper.readValue(json, it)
                } catch (ignored: DatabindException) {}
            }
            throw IllegalArgumentException("Kunne ikke mappe adresse")
        }
    }

    companion object {
        private val mapper = jacksonObjectMapper()
    }
}
