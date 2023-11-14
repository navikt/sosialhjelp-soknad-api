package no.nav.sosialhjelp.soknad.personalia.person

import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AdressebeskyttelseService(
    private val hentPersonClient: HentPersonClient,
    private val mapper: PdlDtoMapper
) {
    @Cacheable(value = ["PDL-harAdressebeskyttelse"], key = "#ident")
    fun harAdressebeskyttelse(ident: String): Boolean {
        val personAdressebeskyttelseDto = hentPersonClient.hentAdressebeskyttelse(ident)
        val graderinger = mapper.personAdressebeskyttelseDtoToGradering(personAdressebeskyttelseDto)
        return graderinger in listOf(Gradering.FORTROLIG, Gradering.STRENGT_FORTROLIG, Gradering.STRENGT_FORTROLIG_UTLAND)
    }
}
