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

        ForsorgerpliktAnsvarComparator(
            original.soknad?.data?.familie?.forsorgerplikt,
            shadow.soknad?.data?.familie?.forsorgerplikt,
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
                return "FIELD name -> '$fieldName': " +
                    "{ ORG -> ${value1?.let { if (typeValues) value1 else "Ulik shadow"} ?: "null" } != " +
                    "SHADOW -> ${value2?.let { if (typeValues) value2 else "Ulik original"} ?: "null" } } "
            }
            return ""
        }
    }
}

interface ProductionComparator {
    fun compare()
}
