package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/okonomiskeOpplysninger")
class UpdateOkonomiController {
    //    @GetMapping
//    fun hentOkonomiOgForventedeVedlegg(): List<OkonomiopplysningerDto> {
//
//    }
}

data class OkonomiopplysningerDto(
    val forventedeVedlegg: List<OkonomiOgVedleggDto>,
)

// TODO Må teste serialisering og de-serialisering av OkonomiType
data class OkonomiOgVedleggDto(
    val type: OkonomiTypeDTO,
    // TODO Nødvendig?
    val gruppe: VedleggGruppe,
)

class OkonomiTypeDTO(okonomiType: OkonomiType) {
    val typeString: String = okonomiType.name
}
