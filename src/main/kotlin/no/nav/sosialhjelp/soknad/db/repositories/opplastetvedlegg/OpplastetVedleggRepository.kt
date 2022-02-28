package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import java.util.Optional

interface OpplastetVedleggRepository {
    fun hentVedlegg(uuid: String?, eier: String): Optional<OpplastetVedlegg>
    fun hentVedleggForSoknad(soknadId: Long, eier: String?): List<OpplastetVedlegg>
    fun opprettVedlegg(opplastetVedlegg: OpplastetVedlegg, eier: String): String
    fun slettVedlegg(uuid: String?, eier: String)
    fun slettAlleVedleggForSoknad(soknadId: Long, eier: String)
    fun hentSamletVedleggStorrelse(soknadId: Long, eier: String): Int
}
