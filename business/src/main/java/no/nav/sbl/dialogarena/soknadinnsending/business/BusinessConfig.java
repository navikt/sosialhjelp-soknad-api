package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.AvbrytAutomatiskSheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.NullstillValgtNavenhetVedKommunesammenslaingSheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.SlettLoggScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandtererImpl;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LagringsScheduler.class,
        SlettLoggScheduler.class,
        DbConfig.class,
        ConsumerConfig.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        ArbeidsforholdService.class,
        ArbeidsforholdTransformer.class,
        OppgaveHandtererImpl.class,
        AvbrytAutomatiskSheduler.class,
        NullstillValgtNavenhetVedKommunesammenslaingSheduler.class,
        SoknadsmottakerService.class,
        SystemdataUpdater.class,
        KontonummerSystemdata.class,
        TelefonnummerSystemdata.class,
        InntektSystemdata.class,
        ArbeidsforholdSystemdata.class,
        BasisPersonaliaSystemdata.class,
        AdresseSystemdata.class,
        FamilieSystemdata.class,
        BostotteSystemdata.class
})
public class BusinessConfig {

}
