package no.nav.sosialhjelp.soknad.v2.livssituasjon

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/utdanning", produces = [MediaType.APPLICATION_JSON_VALUE])
class UtdanningController(
    private val utdanningService: UtdanningService,
) {
    @GetMapping
    fun getUtdanning(
        @PathVariable("soknadId") soknadId: UUID,
    ): UtdanningDto {
        return utdanningService.findUtdanning(soknadId)?.toUtdanningDto() ?: UtdanningDto()
    }

    @PutMapping
    fun updateUtdanning(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: UtdanningInput,
    ): UtdanningDto {
        return when (input) {
            is IkkeStudentInput ->
                utdanningService.updateUtdanning(soknadId, erStudent = false, studentgrad = null)
            is StudentgradInput ->
                utdanningService.updateUtdanning(soknadId, erStudent = true, studentgrad = input.studentgrad)
            else -> throw IllegalArgumentException("Ukjent type for UtdanningInput")
        }.toUtdanningDto()
    }
}

data class UtdanningDto(
    val erStudent: Boolean? = null,
    val studentgrad: Studentgrad? = null,
)

private fun Utdanning.toUtdanningDto() =
    UtdanningDto(
        erStudent = erStudent,
        studentgrad = studentgrad,
    )

@JsonSubTypes(
    JsonSubTypes.Type(IkkeStudentInput::class, name = "IkkeStudent"),
    JsonSubTypes.Type(StudentgradInput::class, name = "Studentgrad"),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "IkkeStudent", schema = IkkeStudentInput::class),
        DiscriminatorMapping(value = "Studentgrad", schema = StudentgradInput::class),
    ],
    subTypes = [IkkeStudentInput::class, StudentgradInput::class],
)
interface UtdanningInput

class IkkeStudentInput : UtdanningInput {
    val erStudent: Boolean = false
}

data class StudentgradInput(
    val studentgrad: Studentgrad?,
) : UtdanningInput
