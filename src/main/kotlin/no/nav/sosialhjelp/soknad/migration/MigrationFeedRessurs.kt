package no.nav.sosialhjelp.soknad.migration

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.migration.dto.ReplicationDto
// import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

// @Controller
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/internal/migration")
@Produces(MediaType.APPLICATION_JSON)
@Timed
class MigrationFeedRessurs(
    private val migrationService: MigrationService
) {

    @GET
    @Path("/feed")
    fun hentFornavn(
        @QueryParam("sistEndretTidspunkt") sistEndretTidspunktString: String?,
    ): ReplicationDto? {
        val sistEndretTidspunkt = sistEndretTidspunktString?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME) } ?: LocalDateTime.MIN
        return migrationService.getNext(sistEndretTidspunkt)
    }
}
