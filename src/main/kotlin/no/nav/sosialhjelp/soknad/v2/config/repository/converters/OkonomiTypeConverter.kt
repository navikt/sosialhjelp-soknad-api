package no.nav.sosialhjelp.soknad.v2.config.repository.converters

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
object OkonomiTypeToStringConverter : Converter<OkonomiType, String> {
    override fun convert(source: OkonomiType): String = source.name
}

@ReadingConverter
object StringToOkonomiTypeConverter : Converter<String, OkonomiType> {
    override fun convert(source: String): OkonomiType = StringToOkonomiTypeMapper.map(source)
}

private object StringToOkonomiTypeMapper {
    private val okonomiTyper = mutableSetOf<OkonomiType>()

    init {
        okonomiTyper.addAll(FormueType.entries)
        okonomiTyper.addAll(InntektType.entries)
        okonomiTyper.addAll(UtgiftType.entries)
    }

    fun map(typeString: String): OkonomiType {
        return okonomiTyper.firstOrNull { it.name == typeString } ?: error("Fant ikke OkonomiType")
    }
}
