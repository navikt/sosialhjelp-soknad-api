package no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.JsonToAdresseObjectMapper
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.IdIsSoknadIdObject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.annotation.Id
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.util.*

data class Brukerdata(
    @Id override val soknadId: UUID,
    val valgtAdresse: AdresseValg? = null,
    val oppholdsadresse: AdresseObject? = null,
    val andreData: MutableMap<BrukerdataKey, BrukerdataValue> = mutableMapOf()
): IdIsSoknadIdObject(soknadId)

data class BrukerdataValue(
    val value: String
)

enum class BrukerdataKey {
    TELEFONNUMMER,
    KOMMENTAR_ARBEIDSFORHOLD,
    KONTONUMMER,
    HVORFOR_SOKE,
    HVA_SOKES_OM,
    BESKRIVELSE_ANNET_BARNEUTGIFTER,
    BESKRIVELSE_ANNET_VERDI,
    BESKRIVELSE_ANNET_SPARING,
    BESKRIVELSE_ANNET_UTBETALING,
    BESKRIVELSE_ANNET_BOUTGIFTER;
}

@WritingConverter
class AdresseObjectToJsonConverter: Converter<AdresseObject, String> {
    private val mapper = jacksonObjectMapper()

    override fun convert(source: AdresseObject): String? {
        return mapper.writeValueAsString(source)
    }
}

@ReadingConverter
class JsonToAdresseObjectConverter: Converter<String, AdresseObject> {
    override fun convert(source: String): AdresseObject {
        return JsonToAdresseObjectMapper.mapAdresseJson(source)
    }
}
