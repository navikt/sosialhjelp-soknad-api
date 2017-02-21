package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.aktivitetbetalingsplan.AktivitetBetalingsplanBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.SlettFeilaktigeGamleSoknaderScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LagringsScheduler.class,
        SlettFeilaktigeGamleSoknaderScheduler.class,
        DbConfig.class,
        AktivitetService.class,
        MaalgrupperService.class,
        PersonaliaBolk.class,
        AktivitetBetalingsplanBolk.class,
        BarnBolk.class,
        ConsumerConfig.class,
        ArbeidsforholdBolk.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        KravdialogInformasjonHolder.class,
        WebSoknadConfig.class,
        ArbeidsforholdService.class,
        ArbeidsforholdTransformer.class
})
public class BusinessConfig {

}
