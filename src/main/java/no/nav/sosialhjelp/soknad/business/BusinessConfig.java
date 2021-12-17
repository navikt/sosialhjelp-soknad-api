package no.nav.sosialhjelp.soknad.business;

import no.nav.sosialhjelp.soknad.business.db.config.DbConfig;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.TextHelpers;
import no.nav.sosialhjelp.soknad.business.service.ServiceConfig;
import no.nav.sosialhjelp.soknad.common.systemdata.SystemdataUpdater;
import no.nav.sosialhjelp.soknad.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        DbConfig.class,
        ConsumerConfig.class,
        ServiceConfig.class,
        SystemdataUpdater.class,
        PdfGenerator.class,
        TextHelpers.class,
        SosialhjelpPdfGenerator.class
})
public class BusinessConfig {

}
