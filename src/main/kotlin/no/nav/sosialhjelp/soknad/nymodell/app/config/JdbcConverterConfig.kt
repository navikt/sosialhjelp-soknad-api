package no.nav.sosialhjelp.soknad.nymodell.app.config

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.GateAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.PostboksAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.UstrukturertAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BegrunnelseKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BeskrivelseAvAnnetKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataKey
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

private val mapper = jacksonObjectMapper()

@Profile("!no-jdbc-converter")
@Configuration
class JdbcConverterConfig : AbstractJdbcConfiguration() {
    override fun userConverters(): MutableList<Converter<*, *>> {
        return mutableListOf(
            JsonToAdresseObjectConverter,
            AdresseObjectToJsonConverter,
            BrukerdataKeyToStringConverter,
            StringToBrukerdataKeyConverter
        )
    }
}

/**
 * For å unngå mange tabeller / en tabell med unødvendige kolonner - serialiseres et adresseobjekt
 * til JSON ved lagring til database
 */
@WritingConverter
object AdresseObjectToJsonConverter : Converter<AdresseObject, String> {
    override fun convert(source: AdresseObject): String = mapper.writeValueAsString(source)
}

/**
 * Deserialisering av Json-adresseobjekt tilbake til Riktig type.
 */
@ReadingConverter
object JsonToAdresseObjectConverter : Converter<String, AdresseObject> {
    override fun convert(source: String): AdresseObject = JsonToAdresseObjectMapper.map(source)
}

/**
 * Converter som serialiserer objekter lagret som (interfacet) BrukerdataKey til String
 */
@WritingConverter
object BrukerdataKeyToStringConverter : Converter<BrukerdataKey, String> {
    override fun convert(source: BrukerdataKey): String = source.name
}

/**
 * Converter som deserialiserer String tilbake til riktig type.
 */
@ReadingConverter
object StringToBrukerdataKeyConverter : Converter<String, BrukerdataKey> {
    override fun convert(source: String): BrukerdataKey = BrukerdataKeyMapper.map(source)
}

/**
 * Mapper en (json-)String tilbake til riktig implementasjon av AdresseObject
 */
private object JsonToAdresseObjectMapper {
    private val mapper = jacksonObjectMapper()

    val adresseTyper = setOf(
        GateAdresseObject::class.java,
        MatrikkelAdresseObject::class.java,
        PostboksAdresseObject::class.java,
        UstrukturertAdresseObject::class.java
    )

    fun map(json: String): AdresseObject {
        adresseTyper.forEach {
            try {
                return mapper.readValue(json, it)
            } catch (ignored: DatabindException) {}
        }
        throw IllegalArgumentException("Kunne ikke mappe adresse")
    }
}

/**
 * Mapper String tilbake til riktig implementasjon av BrukerdataKey
 */
private object BrukerdataKeyMapper {
    // liste med BrukerdataKey for mapping av Streng -> Key fra BrukerdataKeyValue-store
    private val brukerdataKeys: List<BrukerdataKey> = setOf(
        GenerelleDataKey.entries.toList(),
        BeskrivelseAvAnnetKey.entries.toList(),
        BegrunnelseKey.entries.toList()
    ).flatten()

    fun map(name: String) = brukerdataKeys.find { name == it.name }
        ?: throw IllegalStateException("BrukerdataKey finnes ikke!")
}
