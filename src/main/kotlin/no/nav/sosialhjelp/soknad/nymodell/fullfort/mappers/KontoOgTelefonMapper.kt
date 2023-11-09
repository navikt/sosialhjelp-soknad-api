//package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
//import no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.generell.toJsonKilde
//import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
//import java.util.*
//
//class KontoOgTelefonMapper(
//    private val kontonummerRepository: KontonummerRepository,
//    private val telefonnummerRepository: TelefonnummerRepository
//): DomainToJsonMapper {
//    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
//        mapTelefonnummer(soknadId, jsonInternalSoknad)
//        mapKontonummer(soknadId, jsonInternalSoknad)
//    }
//
//    private fun mapTelefonnummer(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
//        val telefonnummer = (telefonnummerRepository.findById(soknadId, Kilde.BRUKER)
//            ?: telefonnummerRepository.findById(soknadId, Kilde.SYSTEM))
//
//        telefonnummer?.let {
//            jsonInternalSoknad.soknad.data.personalia.withTelefonnummer(
//                JsonTelefonnummer()
//                    .withKilde(it.kilde.toJsonKilde())
//            )
//        }
//    }
//
//    private fun mapKontonummer(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
//        val kontonummer = kontonummerRepository.findById(soknadId, Kilde.BRUKER)
//            ?: kontonummerRepository.findById(soknadId, Kilde.SYSTEM)
//
//        kontonummer?.let {
//            jsonInternalSoknad.soknad.data.personalia.withKontonummer(
//                JsonKontonummer()
//                    .withKilde(it.kilde.toJsonKilde())
//                    .withVerdi(it.nummer)
//            )
//        }
//    }
//}
