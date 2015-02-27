package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.DefaultPersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LagringsScheduler.class,
        DbConfig.class,
        DefaultPersonaliaService.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class
})
public class BusinessConfig {

}
