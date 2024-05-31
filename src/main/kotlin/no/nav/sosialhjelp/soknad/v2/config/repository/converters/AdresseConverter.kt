package no.nav.sosialhjelp.soknad.v2.config.repository.converters

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.PostboksAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

private val mapper = jacksonObjectMapper()

@WritingConverter
object AdresseToJsonConverter : Converter<Adresse, String> {
    override fun convert(source: Adresse): String = mapper.writeValueAsString(source)
}

@ReadingConverter
object JsonToAdresseConverter : Converter<String, Adresse> {
    override fun convert(source: String): Adresse = JsonToAdresseMapper.map(source)
}

private object JsonToAdresseMapper {
    val adresseTyper =
        setOf(
            VegAdresse::class.java,
            MatrikkelAdresse::class.java,
            PostboksAdresse::class.java,
            UstrukturertAdresse::class.java,
        )

    fun map(json: String): Adresse {
        adresseTyper.forEach { kotlin.runCatching { return mapper.readValue(json, it) } }
        throw IllegalArgumentException("Kunne ikke mappe adresse")
    }
}
