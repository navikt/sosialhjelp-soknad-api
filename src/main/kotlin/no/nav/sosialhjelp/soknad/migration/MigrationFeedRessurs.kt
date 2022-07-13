package no.nav.sosialhjelp.soknad.migration

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.common.Constants
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.OK
import javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE

@Controller
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/internal/migration")
@Produces(MediaType.APPLICATION_JSON)
open class MigrationFeedRessurs(
    private val migrationService: MigrationService,
    private val unleash: Unleash
) {

    @GET
    @Path("/feed")
    fun getNextSoknadForMigration(
        @QueryParam("sistEndretDato") sistEndretDatoString: String?
    ): Response {
        if (!unleash.isEnabled(MIGRATION_API_ENABLED)) {
            return Response.status(SERVICE_UNAVAILABLE).build()
        }

        val sistEndretDato = sistEndretDatoString?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME) } ?: LocalDateTime.MIN
        val next = migrationService.getNext(sistEndretDato)
        return Response.status(OK).type(MediaType.APPLICATION_JSON_TYPE).entity(next).build()
    }

    /**
     * Endepunkt som skal kunne trigges for å verifisere at antall rader i oracle-db == antall rader i postgres-db
     */
    @GET
    @Path("/sjekksum")
    fun getSjekksum(): Response {
        if (!unleash.isEnabled(MIGRATION_API_ENABLED)) {
            return Response.status(SERVICE_UNAVAILABLE).build()
        }

        val sjekksum = migrationService.getSjekksum()
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(sjekksum).build()
    }

    companion object {
        private const val MIGRATION_API_ENABLED = "sosialhjelp.soknad.migration-api-enabled"
    }
}
