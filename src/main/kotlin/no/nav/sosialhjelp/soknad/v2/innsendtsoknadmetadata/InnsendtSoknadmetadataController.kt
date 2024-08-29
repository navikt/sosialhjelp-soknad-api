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

    @PostMapping("/innsendt-soknad-metadata")
    fun upsertInnsendtSoknadMetadata(@RequestBody innsendtSoknadmetadata: InnsendtSoknadmetadata): ResponseEntity<InnsendtSoknadmetadata> {
        return ResponseEntity.ok(
            innsendtSoknadMetadataService.upsertInnsendtSoknadMetadata(
                innsendtSoknadmetadata.soknadId,
                innsendtSoknadmetadata.personId,
                innsendtSoknadmetadata.sendt_inn_dato,
                innsendtSoknadmetadata.opprettet_dato
            )
        )
    }

    @PutMapping("/innsendt-soknad-metadata")
    fun updateInnsendtSoknadMetadata(@RequestBody innsendtSoknadmetadata: InnsendtSoknadmetadata): ResponseEntity<InnsendtSoknadmetadata> {
        return ResponseEntity.ok(
            innsendtSoknadMetadataService.upsertInnsendtSoknadMetadata(
                innsendtSoknadmetadata.soknadId,
                innsendtSoknadmetadata.personId,
                innsendtSoknadmetadata.sendt_inn_dato,
                innsendtSoknadmetadata.opprettet_dato
            )
        )
    }

//    @DeleteMapping("/innsendt-soknad-metadata/{soknadId}")
//    fun deleteInnsendtSoknadMetadata(@PathVariable soknadId: UUID): ResponseEntity<Void> {
//        innsendtSoknadMetadataService.deleteInnsendtSoknadMetadata(soknadId)
//        return ResponseEntity.noContent().build()
//    }

//    @DeleteMapping("/innsendt-soknad-metadata")
//    fun deleteInnsendtSoknadMetadataEldreEnn(@RequestParam eldreEnn: LocalDateTime): ResponseEntity<Void> {
//        innsendtSoknadMetadataService.deleteAlleEldreEnn(eldreEnn)
//        return ResponseEntity.noContent().build()
//    }
}
