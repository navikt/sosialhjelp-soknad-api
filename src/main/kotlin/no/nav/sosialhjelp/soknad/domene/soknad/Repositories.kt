package no.nav.sosialhjelp.soknad.domene.soknad

import no.nav.sosialhjelp.soknad.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.domene.arbeid.Arbeid
import no.nav.sosialhjelp.soknad.domene.familie.Familie
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SoknadRepository: UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>

@Repository
interface BosituasjonRepository : UpsertRepository<Bosituasjon>, ListCrudRepository<Bosituasjon, UUID>

@Repository
interface ArbeidRepository : UpsertRepository<Arbeid>, ListCrudRepository<Arbeid, UUID>

@Repository
interface FamilieRepository : UpsertRepository<Familie>, ListCrudRepository<Familie, UUID>

@Repository
interface VedleggRepository : ListCrudRepository<Vedlegg, Long> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}
@Repository
interface FilRepository : ListCrudRepository<Fil, Long> {
    fun findAllByVedleggId(vedleggId: Long): List<Fil>
}
