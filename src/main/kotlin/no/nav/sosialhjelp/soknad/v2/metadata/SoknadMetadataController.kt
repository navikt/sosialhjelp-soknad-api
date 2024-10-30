package no.nav.sosialhjelp.soknad.v2.metadata

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// TODO Denne trengs vel ikke ?
@RestController
class SoknadMetadataController(private val soknadMetadataService: SoknadMetadataService) {
    @GetMapping("/innsendt-soknad-metadata/{soknadId}")
    fun getInnsendtSoknadMetadata(
        @PathVariable soknadId: UUID,
    ): ResponseEntity<SoknadMetadata> {
        TODO("Ikke implementert")
    }
}
