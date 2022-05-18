package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggRessurs.Companion.KS_MELLOMLAGRING_ENABLED
import org.springframework.stereotype.Component

@Component
class MellomlagringService(
    private val unleash: Unleash,
    private val kommuneInfoService: KommuneInfoService
) {

    fun getAllVedlegg(behandlingsId: String): List<MellomlagretVedleggMetadata> {
        // todo implement
        return emptyList()
    }

    fun getVedlegg(vedleggId: String): MellomlagretVedlegg? {
        // todo implement
        return null
    }

    fun getVedlegg(behandlingsId: String, vedleggId: String): MellomlagretVedlegg? {
        // todo implement
        return null
    }

    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        originalfilnavn: String
    ): MellomlagretVedleggMetadata {
        // todo implement
        return MellomlagretVedleggMetadata(filnavn = "filnavn", filId = "uuid")
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String, vedleggId: String) {
        // todo implement
    }

    fun deleteVedlegg(behandlingsId: String, vedleggId: String) {
        // todo implement
    }

    fun deleteAllVedlegg(behandlingsId: String) {
        // todo implement
    }

    fun erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        return unleash.isEnabled(KS_MELLOMLAGRING_ENABLED, false) && soknadSkalSendesMedDigisosApi(soknadUnderArbeid)
    }

    private fun soknadSkalSendesMedDigisosApi(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        if (soknadUnderArbeid.erEttersendelse) {
            return false
        }
        val kommunenummer = soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            ?: throw IllegalStateException("Kommunenummer ikke funnet for JsonInternalSoknad.soknad.mottaker.kommunenummer")

        return when (kommuneInfoService.kommuneInfo(kommunenummer)) {
            KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException("Mellomlagring av vedlegg er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.")
            }
            KommuneStatus.MANGLER_KONFIGURASJON, KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> false
            KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> true
            KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException("Sending til kommune $kommunenummer er midlertidig utilgjengelig.")
            }
        }
    }
}

data class MellomlagretVedleggMetadata(
    val filnavn: String,
    val filId: String
)

data class MellomlagretVedlegg(
    val filnavn: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MellomlagretVedlegg

        if (filnavn != other.filnavn) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filnavn.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
