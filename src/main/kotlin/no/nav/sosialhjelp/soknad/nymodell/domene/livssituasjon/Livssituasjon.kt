package no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon

import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Botype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Stillingstype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Studentgrad
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.IdIsSoknadIdObject
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.UuidAsIdObject
import org.springframework.data.annotation.Id
import java.util.*

data class Arbeidsforhold (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val orgnummer: String? = null,
    val arbeidsgivernavn: String,
    val fraOgMed: String? = null,
    val tilOgMed: String? = null,
    val stillingsprosent: Int? = null,
    val stillingstype: Stillingstype? = null
): UuidAsIdObject

data class Bosituasjon (
    @Id override val soknadId: UUID,
    var botype: Botype?,
    var antallPersoner: Int
): IdIsSoknadIdObject(soknadId)

data class Utdanning (
    @Id override val soknadId: UUID,
    val erStudent: Boolean,
    val studentGrad: Studentgrad
): IdIsSoknadIdObject(soknadId)