package no.nav.sosialhjelp.soknad.scheduled.leaderelection

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.reactive.function.client.WebClient
import java.net.InetAddress.getLocalHost
import java.time.LocalDateTime

interface LeaderElection {
    fun isLeader(): Boolean
}

class LeaderElectionImpl(
    webClientBuilder: WebClient.Builder,
) : LeaderElection {

    private val electorPath: String? = System.getenv(ELECTOR_PATH)
    private val webClient: WebClient = unproxiedWebClientBuilder(webClientBuilder, "http://$electorPath").build()

    private var hostname: String = getLocalHost().hostName
    private var leader: String? = null
    private var lastCallTime = LocalDateTime.MIN

    override fun isLeader(): Boolean {
        if (electorPath == null) {
            log.warn("LeaderElection - manglende systemvariabel=$ELECTOR_PATH.")
            return true
        }
        val now = LocalDateTime.now()
        if (leader == null || lastCallTime.isBefore(now.minusMinutes(2))) {
            try {
                val response = webClient.get()
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .block()
                leader = jacksonObjectMapper().readTree(response).get("name").asText()
                lastCallTime = now
            } catch (e: Exception) {
                log.warn("LeaderElection - kunne ikke bestemme lederpod. ${e.message}", e)
                return true
            }
        }
        return hostname == leader
    }

    companion object {
        private const val ELECTOR_PATH = "ELECTOR_PATH"
        private val log = getLogger(LeaderElectionImpl::class.java)
    }
}

class NoLeaderElection : LeaderElection {

    override fun isLeader(): Boolean {
        return true
    }
}
