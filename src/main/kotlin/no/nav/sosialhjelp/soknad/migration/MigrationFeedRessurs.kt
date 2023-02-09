package no.nav.sosialhjelp.soknad.migration

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.migration.dto.ReplicationDto
import no.nav.sosialhjelp.soknad.migration.dto.SjekksumDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

@RestController
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/internal/migration", produces = [MediaType.APPLICATION_JSON_VALUE])
class MigrationFeedRessurs(
    private val migrationService: MigrationService,
    private val unleash: Unleash,
) {

    @GetMapping("/feed")
    fun getNextSoknadForMigration(
        @RequestParam("sistEndretDato") sistEndretDatoString: String?,
    ): ResponseEntity<ReplicationDto> {
        if (!unleash.isEnabled(MIGRATION_API_ENABLED)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val sistEndretDato = sistEndretDatoString?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME) } ?: LocalDateTime.MIN
        val next = migrationService.getNext(sistEndretDato)
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(next)
    }

    /**
     * Endepunkt som skal kunne trigges for å verifisere at antall rader i oracle-db == antall rader i postgres-db
     */
    @GetMapping("/sjekksum")
    fun getSjekksum(): ResponseEntity<SjekksumDto> {
        if (!unleash.isEnabled(MIGRATION_API_ENABLED)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val sjekksum = migrationService.getSjekksum()
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sjekksum)
    }

    companion object {
        private const val MIGRATION_API_ENABLED = "sosialhjelp.soknad.migration-api-enabled"
    }
}
