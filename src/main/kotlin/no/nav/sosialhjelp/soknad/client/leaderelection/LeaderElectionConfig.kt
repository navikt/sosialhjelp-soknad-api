package no.nav.sosialhjelp.soknad.client.leaderelection

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class LeaderElectionConfig(
    private val nonProxiedWebClientBuilder: WebClient.Builder
) {

    @Profile("leader-election")
    @Bean
    open fun leaderElection(): LeaderElection {
        return LeaderElectionImpl(nonProxiedWebClientBuilder)
    }

    @Profile("!leader-election")
    @Bean
    open fun noLeaderElection(): LeaderElection {
        return NoLeaderElection()
    }
}
