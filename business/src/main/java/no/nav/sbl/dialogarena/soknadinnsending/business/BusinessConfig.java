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
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdTransformer;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGeneratorConfig;
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
        UtbetalingerFraNavSystemdata.class,
        ArbeidsforholdSystemdata.class,
        BasisPersonaliaSystemdata.class,
        AdresseSystemdata.class,
        FamilieSystemdata.class,
        BostotteSystemdata.class,
        PdfGeneratorConfig.class
})
public class BusinessConfig {

}
