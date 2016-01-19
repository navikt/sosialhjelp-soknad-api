package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetBetalingsplanService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MaalgrupperService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LagringsScheduler.class,
        DbConfig.class,
        AktivitetService.class,
        MaalgrupperService.class,
        PersonaliaService.class,
        AktivitetBetalingsplanService.class,
        BarnService.class,
        ConsumerConfig.class,
        ArbeidsforholdService.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        KravdialogInformasjonHolder.class,
        WebSoknadConfig.class
})
public class BusinessConfig {

}
