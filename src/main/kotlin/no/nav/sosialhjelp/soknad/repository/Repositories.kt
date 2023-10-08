package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.model.Fil
import no.nav.sosialhjelp.soknad.model.Soknad
import no.nav.sosialhjelp.soknad.model.Vedlegg
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface CommonRepository<T> : ListCrudRepository<T,Long> {
    fun findBySoknadId(uuid: UUID): Optional<T>
    fun existsBySoknadId(uuid: UUID): Boolean
}

@Repository
interface SoknadRepository : CommonRepository<Soknad>
@Repository
interface BosituasjonRepository : CommonRepository<Bosituasjon>
@Repository
interface VedleggRepository : ListCrudRepository<Vedlegg, Long> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}
@Repository
interface FilRepository : ListCrudRepository<Fil, Long> {
    fun findAllByVedleggId(vedleggId: Long): List<Fil>
}

//interface FindSoknadRepository {
//    fun findBySoknadId(soknadId: UUID): Soknad
//}
//
//class FindSoknadRepositoryImpl (val ctx: ApplicationContext): FindSoknadRepository {
//    override fun findBySoknadId(soknadId: UUID): Soknad {
//        val soknadRepository = ctx.getBean(SoknadRepository::class.java)
//        return soknadRepository.findBySoknadId(soknadId)
//    }
//
//
