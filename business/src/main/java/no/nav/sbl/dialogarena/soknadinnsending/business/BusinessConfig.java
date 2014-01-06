package no.nav.sbl.dialogarena.soknadinnsending.business;

import org.springframework.context.Phased;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
public class BusinessConfig implements Phased {
    @Override
    public int getPhase() {
        System.out.println("PHADES BUSINESS");
        return 20;
    }
}
