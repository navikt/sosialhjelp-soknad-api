package no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.FilMeta
import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.Vedlegg
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VedleggRepository : ListCrudRepository<Vedlegg, Long> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}
@Repository
interface FilMetaRepository : ListCrudRepository<FilMeta, Long> {
    fun findAllByVedleggId(vedleggId: Long): List<FilMeta>
}