package no.nav.sosialhjelp.soknad.domene

import no.nav.sosialhjelp.soknad.domene.soknad.CommonSoknadModel
import org.springframework.data.jdbc.core.JdbcAggregateTemplate


// Samme signatur som CrudRepository slik at denne overrides når et
// repository-interface implementerer begge
interface UuidAsKeyRepository<T: CommonSoknadModel> {
    fun <S : T> save(s: S): S
}
class UuidAsKeyRepositoryImpl<T: CommonSoknadModel>(
    private val jdbcAggregateTemplate: JdbcAggregateTemplate
): UuidAsKeyRepository<T> {
    override fun <S : T> save(s: S): S {
        return if (jdbcAggregateTemplate.existsById(s.id, s.javaClass)) {
            jdbcAggregateTemplate.update(s)
        } else {
            jdbcAggregateTemplate.insert(s)
        }
    }
}
