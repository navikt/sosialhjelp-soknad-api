package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.soknadinnsending.business.kodeverk.KodeverkConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.SosialhjelpArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.AvbrytAutomatiskSheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandtererImpl;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.soknadinnsending.business.sosialhjelp.SosialhjelpKontaktBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling.UtbetalingBolk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;

@Configuration
@Import({
        LagringsScheduler.class,
        DbConfig.class,
        AktivitetService.class,
        MaalgrupperService.class,
        PersonaliaBolk.class,
        BarnBolk.class,
        ConsumerConfig.class,
        ArbeidsforholdBolk.class,
        SosialhjelpArbeidsforholdBolk.class,
        UtbetalingBolk.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        KravdialogInformasjonHolder.class,
        WebSoknadConfig.class,
        ArbeidsforholdService.class,
        ArbeidsforholdTransformer.class,
        SosialhjelpKontaktBolk.class,
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
        //KodeverkConfig.class
})
public class BusinessConfig {

}
