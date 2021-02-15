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
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.ArbeidsforholdSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BostotteSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.FamilieSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.KontonummerSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.TelefonnummerSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.UtbetalingerFraNavSystemdata;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGeneratorConfig;
import no.nav.sosialhjelp.soknad.consumer.ConsumerConfig;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.ArbeidsforholdService;
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
        SkattetatenSystemdata.class,
        PdfGeneratorConfig.class
})
public class BusinessConfig {

}
