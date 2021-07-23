package no.nav.sosialhjelp.soknad.business.db.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("(mock-alt|local|test)")
public class InMemoryDbConfiguration {


}
