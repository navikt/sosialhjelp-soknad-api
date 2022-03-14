package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OpplastetVedleggTypeTest {

    @Test
    fun vedleggTypeObjekterMedSammeTypeOgTilleggsinfoErLike() {
        val vedleggType = OpplastetVedleggType(TYPE)
        val likVedleggType = OpplastetVedleggType(TYPE)
        assertThat(vedleggType == likVedleggType).isTrue
    }

    @Test
    fun vedleggTypeObjekterMedSammeTypeOgTilleggsinfoHarSammeHashVerdi() {
        val vedleggTypeList: MutableList<OpplastetVedleggType> = mutableListOf(
            OpplastetVedleggType(TYPE),
            OpplastetVedleggType(TYPE)
        )
        val vedleggTyper: MutableSet<OpplastetVedleggType> = HashSet()
        vedleggTypeList.removeIf { type: OpplastetVedleggType ->
            !vedleggTyper.add(type)
        }
        assertThat(vedleggTyper).hasSize(1)
        assertThat(vedleggTypeList).hasSize(1)
    }

    @Test
    fun vedleggTypeObjekterMedSammeTypeOgUlikTilleggsinfoErUlike() {
        val vedleggType = OpplastetVedleggType(TYPE)
        val likVedleggType = OpplastetVedleggType(TYPE2)
        assertThat(vedleggType == likVedleggType).isFalse
    }

    @Test
    fun vedleggTypeObjekterMedUlikTypeOgSammeTilleggsinfoErUlike() {
        val vedleggType = OpplastetVedleggType(TYPE)
        val likVedleggType = OpplastetVedleggType(TYPE2)
        assertThat(vedleggType == likVedleggType).isFalse
    }

    companion object {
        private const val TYPE = "bostotte|kontooversikt"
        private const val TYPE2 = "annetboutgift|brukskonto"
    }
}
