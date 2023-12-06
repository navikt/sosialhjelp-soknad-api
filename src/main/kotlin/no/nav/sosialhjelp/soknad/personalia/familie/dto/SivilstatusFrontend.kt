package no.nav.sosialhjelp.soknad.personalia.familie.dto

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status

data class SivilstatusFrontend(
    val kildeErSystem: Boolean?,
    val sivilstatus: Status?,
    val ektefelle: EktefelleFrontend?,
    val harDiskresjonskode: Boolean?,
    val borSammenMed: Boolean?,
    val erFolkeregistrertSammen: Boolean?,
)

enum class SivilstatusDatatype {
    SYSTEM_UGIFT, SYSTEM_GIFT, SYSTEM_GIFT_GRADERT, BRUKER
}

@Schema(
    subTypes = [SivilstatusGiftResponse::class, SivilstatusUgiftResponse::class, SivilstatusBrukerResponse::class, SivilstatusGradertResponse::class],
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "SYSTEM_GIFT", schema = SivilstatusGiftResponse::class),
        DiscriminatorMapping(value = "SYSTEM_GIFT_GRADERT", schema = SivilstatusGradertResponse::class),
        DiscriminatorMapping(value = "SYSTEM_UGIFT", schema = SivilstatusUgiftResponse::class),
        DiscriminatorMapping(value = "BRUKER", schema = SivilstatusBrukerResponse::class),
    ]
)
sealed class SivilstatusResponse(
    @Schema(required = true)
    val type: SivilstatusDatatype
)

/**
 * Sivilstatus er gift, men ektefelle har adressebeskyttelse
 */
class SivilstatusGradertResponse : SivilstatusResponse(SivilstatusDatatype.SYSTEM_GIFT_GRADERT)

data class SivilstatusGiftResponse(
    @Schema(required = true)
    val ektefelle: EktefelleFrontend,
    val erFolkeregistrertSammen: Boolean,
    @Schema(deprecated = true, description = "Kun her for bakoverkompatibilitet. Kan fjernes etter frontend upgrade")
    val kildeErSystem: Boolean = true,
    @Schema(deprecated = true, description = "Kun her for bakoverkompatibilitet. Kan fjernes etter frontend upgrade")
    val sivilstatus: Status = Status.GIFT,
) : SivilstatusResponse(SivilstatusDatatype.SYSTEM_GIFT)

/**
 * Dekker alle sivilstatuser som ikke er gift
 **/
data class SivilstatusUgiftResponse(
    val sivilstatus: Status,
    @Schema(deprecated = true, description = "Kun her for bakoverkompatibilitet. Kan fjernes etter frontend upgrade")
    val kildeErSystem: Boolean = true,
) : SivilstatusResponse(SivilstatusDatatype.SYSTEM_UGIFT)

data class SivilstatusBrukerResponse(
    val sivilstatus: Status? = null,
    val ektefelle: EktefelleFrontend = EktefelleFrontend(),
    val borSammenMed: Boolean? = null,
    @Schema(deprecated = true, description = "Kun her for bakoverkompatibilitet. Kan fjernes etter frontend upgrade")
    val kildeErSystem: Boolean = false,
) : SivilstatusResponse(SivilstatusDatatype.BRUKER)

data class EktefelleFrontend(
    val navn: NavnFrontend = NavnFrontend(),
    val fodselsdato: String? = null,
    val personnummer: String? = null,
)
