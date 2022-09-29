package no.nav.sosialhjelp.soknad.scheduled.leaderelection

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class LeaderElectionConfig {

    @Profile("leader-election")
    @Bean
    open fun leaderElection(webClientBuilder: WebClient.Builder): LeaderElection {
        return LeaderElectionImpl(webClientBuilder)
    }

    @Profile("!leader-election")
    @Bean
    open fun noLeaderElection(): LeaderElection {
        return NoLeaderElection()
    }
}
