package no.nav.sosialhjelp.soknad.v2.innsendtsoknadmetadata

@RestControllerv2
class InnsendtSoknadmetadataController(private val innsendtSoknadMetadataService: InnsendtSoknadMetadataService) {

    @GetMapping("/innsendt-soknad-metadata/{soknadId}")
    fun getInnsendtSoknadMetadata(@PathVariable soknadId: UUID): ResponseEntity<InnsendtSoknadmetadata> {
        return innsendtSoknadMetadataService.findInnsendtSoknadMetadata(soknadId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/innsendt-soknad-metadata")
    fun upsertInnsendtSoknadMetadata(@RequestBody innsendtSoknadmetadata: InnsendtSoknadmetadata): ResponseEntity<InnsendtSoknadmetadata> {
        return ResponseEntity.ok(innsendtSoknadMetadataService.upsertInnsendtSoknadMetadata(innsendtSoknadmetadata))
    }

    @PutMapping("/innsendt-soknad-metadata")
    fun updateInnsendtSoknadMetadata(@RequestBody innsendtSoknadmetadata: InnsendtSoknadmetadata): ResponseEntity<InnsendtSoknadmetadata> {
        return ResponseEntity.ok(innsendtSoknadMetadataService.updateInnsendtSoknadMetadata(innsendtSoknadmetadata))
    }

    @DeleteMapping("/innsendt-soknad-metadata/{soknadId}")
    fun deleteInnsendtSoknadMetadata(@PathVariable soknadId: UUID): ResponseEntity<Void> {
        innsendtSoknadMetadataService.deleteInnsendtSoknadMetadata(soknadId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/innsendt-soknad-metadata")
    fun deleteInnsendtSoknadMetadataEldreEnn(@RequestParam eldreEnn: LocalDateTime): ResponseEntity<Void> {
        innsendtSoknadMetadataService.deleteAlleEldreEnn(eldreEnn)
        return ResponseEntity.noContent().build()
    }
}
