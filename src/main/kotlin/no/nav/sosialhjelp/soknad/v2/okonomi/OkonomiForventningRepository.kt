package no.nav.sosialhjelp.soknad.v2.okonomi

import java.util.UUID

interface OkonomiForventningRepository {
    fun findOpplysningTypesBySoknadId(soknadId: UUID): List<String>

    fun setExpectationForOpplysningType(
        soknadId: UUID,
        opplysningType: String,
        isExpected: Boolean,
    )
}
