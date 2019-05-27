package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.AvbrytAutomatiskSheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandtererImpl;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LagringsScheduler.class,
        DbConfig.class,
        AktivitetService.class,
        MaalgrupperService.class,
        ConsumerConfig.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        KravdialogInformasjonHolder.class,
        WebSoknadConfig.class,
        ArbeidsforholdService.class,
        ArbeidsforholdTransformer.class,
        OppgaveHandtererImpl.class,
        AvbrytAutomatiskSheduler.class,
        SoknadsmottakerService.class,
        SystemdataUpdater.class,
        KontonummerSystemdata.class,
        TelefonnummerSystemdata.class,
        ArbeidsforholdSystemdata.class,
        BasisPersonaliaSystemdata.class,
        AdresseSystemdata.class,
        FamilieSystemdata.class,
        InntektSystemdata.class
})
public class BusinessConfig {

}
