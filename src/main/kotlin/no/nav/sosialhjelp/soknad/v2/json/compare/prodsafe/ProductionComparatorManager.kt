package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad

class ProductionComparatorManager(
    val original: JsonInternalSoknad,
    val shadow: JsonInternalSoknad,
) {
    fun compareSpecificFields() {
        OkonomiComparator(
            original.soknad.data.okonomi,
            shadow.soknad.data.okonomi,
        ).compare()

        SivilstatusComparator(
            original.soknad?.data?.familie?.sivilstatus,
            shadow.soknad?.data?.familie?.sivilstatus,
        ).compare()

        VedleggComparator(
            original.vedlegg?.vedlegg ?: emptyList(),
            shadow.vedlegg?.vedlegg ?: emptyList(),
        ).compare()
    }

    companion object {
        val jsonMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

        fun compareStrings(
            value1: String?,
            value2: String?,
            fieldName: String,
            typeValues: Boolean = false,
        ): String {
            if (value1 != value2) {
                return "Field: $fieldName:\n\n" +
                    "Original: ${if (typeValues) value1 else "Ulik shadow"}\n\n" +
                    "Shadow: ${if (typeValues) value2 else "Ulik original"}"
            }
            return ""
        }
    }
}

interface ProductionComparator {
    fun compare()
}
