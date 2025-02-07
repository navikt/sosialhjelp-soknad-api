package no.nav.sosialhjelp.soknad.utdanning

import no.nav.sosialhjelp.soknad.v2.livssituasjon.IkkeStudentInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.StudentgradInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.UtdanningController
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtdanningProxy(private val utdanningController: UtdanningController) {
    fun getUtdanning(soknadId: String): UtdanningFrontend {
        return utdanningController.getUtdanning(UUID.fromString(soknadId))
            .let { utdanning ->
                UtdanningFrontend(
                    erStudent = utdanning.erStudent,
                    studengradErHeltid = utdanning.studentgrad?.let { it == Studentgrad.HELTID },
                )
            }
    }

    fun updateUtdanning(
        soknadId: String,
        utdanningFrontend: UtdanningFrontend,
    ) {
        val input =
            utdanningFrontend.erStudent?.let { erStudent ->

                when {
                    erStudent -> {
                        StudentgradInput(
                            studentgrad =
                                utdanningFrontend.studengradErHeltid?.let {
                                    if (it) Studentgrad.HELTID else Studentgrad.DELTID
                                },
                        )
                    }

                    else -> IkkeStudentInput()
                }
            } ?: return

        utdanningController.updateUtdanning(
            soknadId = UUID.fromString(soknadId),
            input = input,
        )
    }
}
