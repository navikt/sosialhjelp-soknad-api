package no.nav.sosialhjelp.soknad.v2.livssituasjon

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/utdanning", produces = [MediaType.APPLICATION_JSON_VALUE])
class UtdanningController(
    private val livssituasjonService: LivssituasjonService
) {
    @GetMapping
    fun getUtdanning(@PathVariable("soknadId") soknadId: UUID): UtdanningDto? {
        return livssituasjonService.getLivssituasjon(soknadId)?.utdanning?.toUtdanningDto()
    }

    @PutMapping
    fun updateUtdanning(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: UtdanningInput
    ): UtdanningDto {
        return when (input) {
            is IkkeStudentInput ->
                livssituasjonService.updateUtdanning(soknadId, erStudent = false, studentgrad = null)
            is StudentgradInput ->
                livssituasjonService.updateUtdanning(soknadId, erStudent = true, studentgrad = input.studentgrad)
            else -> throw IllegalArgumentException("Ukjent type for UtdanningInput")
        }.toUtdanningDto()
    }
}

data class UtdanningDto(
    val erStudent: Boolean? = null,
    val studentgrad: Studentgrad? = null
)

private fun Utdanning.toUtdanningDto() = UtdanningDto(
    erStudent = erStudent,
    studentgrad = studentgrad
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY
)
@JsonSubTypes(
    JsonSubTypes.Type(IkkeStudentInput::class),
    JsonSubTypes.Type(StudentgradInput::class)
)
interface UtdanningInput

data class IkkeStudentInput(
    val verdi: Int = 1
) : UtdanningInput

data class StudentgradInput(
    val studentgrad: Studentgrad
) : UtdanningInput
