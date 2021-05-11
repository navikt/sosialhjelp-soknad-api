package no.nav.sosialhjelp.soknad.business;

import no.nav.sosialhjelp.soknad.business.batch.AvbrytAutomatiskSheduler;
import no.nav.sosialhjelp.soknad.business.batch.LagringsScheduler;
import no.nav.sosialhjelp.soknad.business.batch.NullstillValgtNavenhetVedKommunesammenslaingSheduler;
import no.nav.sosialhjelp.soknad.business.batch.SlettLoggScheduler;
import no.nav.sosialhjelp.soknad.business.batch.SlettSoknadUnderArbeidScheduler;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.OppgaveHandtererImpl;
import no.nav.sosialhjelp.soknad.business.db.DbConfig;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGeneratorConfig;
import no.nav.sosialhjelp.soknad.business.service.ServiceConfig;
import no.nav.sosialhjelp.soknad.business.service.SoknadsmottakerService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SystemdataUpdater;
import no.nav.sosialhjelp.soknad.business.service.systemdata.AdresseSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.ArbeidsforholdSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.BostotteSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.FamilieSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.KontonummerSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.TelefonnummerSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.UtbetalingerFraNavSystemdata;
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
        SlettSoknadUnderArbeidScheduler.class,
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
