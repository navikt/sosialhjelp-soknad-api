package no.nav.sosialhjelp.soknad.v2.familie

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Unprotected
@RequestMapping("/soknad/{soknadId}/familie", produces = [MediaType.APPLICATION_JSON_VALUE])
class FamilieController(
    private val familieService: FamilieService,
) {
    @GetMapping
    fun getFamilie(@PathVariable soknadId: UUID): FamilieDto? = familieService.findFamilie(soknadId)?.toDto()
}

data class FamilieDto(
    val harForsorgerplikt: Boolean? = null,
    val barnebidrag: Barnebidrag? = null,
    val sivilstatus: Sivilstatus? = null,
    val ansvar: List<BarnDto> = emptyList(),
    val ektefelle: EktefelleDto? = null,
)

data class EktefelleInput(
    val personId: String?,
    val navn: Navn,
    val fodselsdato: String? = null,
    val borSammen: Boolean? = null,
)

data class BarnDto(
    val uuid: UUID,
    val navn: Navn?,
    val fodselsdato: String?,
    val borSammen: Boolean? = null,
    val folkeregistrertSammen: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null,
)

data class EktefelleDto(
    val personId: String?,
    val navn: Navn?,
    val fodselsdato: String?,
    val harDiskresjonskode: Boolean? = null,
    val folkeregistrertMedEktefelle: Boolean? = null,
    val borSammen: Boolean? = null,
)

data class BarnInput(
    val uuid: UUID?,
    val personId: String? = null,
    val deltBosted: Boolean? = null,
)

fun Barn.toDto() = BarnDto(familieKey, navn, fodselsdato, borSammen, folkeregistrertSammen, deltBosted, samvarsgrad)

fun BarnInput.toDomain() = Barn(uuid ?: UUID.randomUUID(), personId, deltBosted = deltBosted)

fun Ektefelle.toDto() = EktefelleDto(personId, navn, fodselsdato, folkeregistrertMedEktefelle, borSammen)

fun EktefelleInput.toDomain() = Ektefelle(navn, fodselsdato, personId, borSammen = borSammen, kildeErSystem = false)

fun Familie.toDto() = FamilieDto(
    harForsorgerplikt,
    barnebidrag,
    sivilstatus,
    ansvar.values.map { it.toDto() },
    if (sivilstatus == Sivilstatus.GIFT) ektefelle?.toDto() else null
)
