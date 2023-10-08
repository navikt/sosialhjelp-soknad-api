package no.nav.sosialhjelp.soknad.service

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.repository.BosituasjonRepository
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

//@Service
//class BosituasjonService (
//    val bosituasjonRepository: BosituasjonRepository
//) {
//
//    fun oppdaterBosituasjon(bosituasjon: Bosituasjon) {
//
//        // eksempel på hvordan UUID som @Id skaper ekstra kodelinjer
//        with (bosituasjon) {
//            bosituasjonRepository.findById(soknadId).getOrNull()?.let {
//                bosituasjonRepository.save(it)
//            }
//                ?: bosituasjonRepository.insert(bosituasjon)
//        }
//    }
//}