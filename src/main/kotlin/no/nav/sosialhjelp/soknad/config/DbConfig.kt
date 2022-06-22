package no.nav.sosialhjelp.soknad.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.Clock

@Configuration
@EnableTransactionManagement
open class DbConfig {

    @Bean
    open fun clock(): Clock {
        return Clock.systemDefaultZone()
    }
}
