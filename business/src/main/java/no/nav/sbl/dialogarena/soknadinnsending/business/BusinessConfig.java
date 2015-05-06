package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.DefaultArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LagringsScheduler.class,
        DbConfig.class,
        PersonaliaService.class,
        BarnService.class,
        ConsumerConfig.class,
        DefaultArbeidsforholdService.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        KravdialogInformasjonConfig.class,
        WebSoknadConfig.class
})
public class BusinessConfig {

}
