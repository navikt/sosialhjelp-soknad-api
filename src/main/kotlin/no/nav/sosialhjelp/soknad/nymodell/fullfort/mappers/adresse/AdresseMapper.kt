//package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.adresse
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
//import no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.DomainToJsonMapper
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.Adresse
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.repository.AdresseRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
//import org.springframework.stereotype.Component
//import java.util.*
//
//@Component
//class AdresseMapper(
//    private val adresseRepository: AdresseRepository,
//    private val soknadRepository: SoknadRepository
//): DomainToJsonMapper {
//
//    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
//
//        val adresseValg = soknadRepository.findById(soknadId).get().adresseValg
//        val adresser = adresseRepository.findAllBySoknadId(soknadId)
//
//        mapFolkeregistrertToJsonInternalSoknad(adresser.find { it.id.typeAdressevalg == AdresseValg.FOLKEREGISTRERT }, jsonInternalSoknad)
//        mapOppholdsAdresse(adresser.find { it.id.typeAdressevalg == adresseValg }, jsonInternalSoknad)
//
//        jsonInternalSoknad.soknad.data.personalia.apply {
//            withPostadresse(oppholdsadresse)
//        }
//    }
//
//    private fun mapFolkeregistrertToJsonInternalSoknad(adresse: Adresse?, jsonInternalSoknad: JsonInternalSoknad) {
//        jsonInternalSoknad.soknad.data.personalia
//            .withFolkeregistrertAdresse(adresse?.toTypedJsonAdresse())
//    }
//
//    private fun mapOppholdsAdresse(adresse: Adresse?, jsonInternalSoknad: JsonInternalSoknad) {
//        jsonInternalSoknad.soknad.data.personalia
//            .withOppholdsadresse(adresse?.toTypedJsonAdresse())
//            .apply {
//                oppholdsadresse.adresseValg = adresse?.toAdresseValg()
//            }
//    }
//}
