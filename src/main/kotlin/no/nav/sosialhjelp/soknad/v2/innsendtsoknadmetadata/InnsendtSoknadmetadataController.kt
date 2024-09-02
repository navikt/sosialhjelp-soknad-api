package no.nav.sosialhjelp.soknad.v2.innsendtsoknadmetadata

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class InnsendtSoknadmetadataController(private val innsendtSoknadMetadataService: InnsendtSoknadMetadataService) {

    @GetMapping("/innsendt-soknad-metadata/{soknadId}")
    fun getInnsendtSoknadMetadata(@PathVariable soknadId: UUID): ResponseEntity<InnsendtSoknadmetadata> {
        return innsendtSoknadMetadataService.findInnsendtSoknadMetadata(soknadId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

}
