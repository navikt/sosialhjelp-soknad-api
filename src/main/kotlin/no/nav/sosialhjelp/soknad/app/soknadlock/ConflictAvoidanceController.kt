package no.nav.sosialhjelp.soknad.app.soknadlock

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/internal/conflictAvoidance")
class ConflictAvoidanceController(
    private val soknadLockManager: SoknadLockManager
) {
    @GetMapping
    fun getStatus(): ConflictAvoidanceResponse = ConflictAvoidanceResponse(soknadLockManager.enabled, soknadLockManager.getNumLocks())

    @PutMapping
    fun setStatus(@RequestBody request: ConflictAvoidanceRequest) =
        ConflictAvoidanceResponse(soknadLockManager.apply { enabled = request.enabled }.enabled, soknadLockManager.getNumLocks())

    data class ConflictAvoidanceRequest(val enabled: Boolean)

    data class ConflictAvoidanceResponse(val enabled: Boolean, val numLocks: Int)
}
