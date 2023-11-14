package no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata

import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.common.BubbleRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.common.SoknadBubble
import no.nav.sosialhjelp.soknad.nymodell.domene.common.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BrukerdataRepository: UpsertRepository<Brukerdata>, BubbleRepository<Brukerdata>

data class Brukerdata(
    @Id override val soknadId: UUID,
    var valgtAdresse: AdresseValg? = null,
    var oppholdsadresse: AdresseObject? = null,
    var samtykker: MutableMap<SamtykkeType, Samtykke> = mutableMapOf(),
    @Embedded.Nullable
    val keyValueStore: BrukerdataKeyValueStore = BrukerdataKeyValueStore()
): SoknadBubble(soknadId) {
    val keyValueStoreMap get() = keyValueStore.getReadMap()
    val keyValueStoreSet get() = keyValueStore.getReadSet()
}

data class Samtykke(
    val verdi: Boolean,
    val bekreftelsesdato: LocalDate
)

enum class SamtykkeType(tittel: String) {
    BOSTOTTE_SAMTYKKE("inntekt.bostotte.samtykke"),
    UTBETALING_SKATTEETATEN_SAMTYKKE("utbetalinger.skattbar.samtykke");
}

// Wrapper-klasse for brukerdata-key-values
class BrukerdataKeyValueStore(
    private val brukerdataKeyValueSet: MutableSet<BrukerdataKeyValue> = mutableSetOf()
) {
    fun getReadSet(): Set<BrukerdataKeyValue> = brukerdataKeyValueSet
    fun getReadMap(): Map<BrukerdataKey, String> = brukerdataKeyValueSet.associate { it.key to it.value }
    fun update(key: BrukerdataKey, value: String) {
        findKV(key)?.let { it.value = value } ?: brukerdataKeyValueSet.add(BrukerdataKeyValue(key, value))
    }
    fun getValue(key: BrukerdataKey) = findKV(key)?.value
    fun delete(key: BrukerdataKey) { brukerdataKeyValueSet.remove(findKV(key)) }
    private fun findKV(key: BrukerdataKey) = brukerdataKeyValueSet.find { it.key == key }
}

data class BrukerdataKeyValue(
    val key: BrukerdataKey,
    var value: String
)
