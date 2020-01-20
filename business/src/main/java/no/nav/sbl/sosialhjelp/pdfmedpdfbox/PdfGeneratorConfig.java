package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.soap.Text;
import java.io.IOException;

@Configuration
public class PdfGeneratorConfig {

    @Bean
    public PdfGenerator pdfGenerator() throws IOException {
        return new PdfGenerator();
    }

    @Bean
    public SosialhjelpPdfGenerator sosialhjelpPdfGenerator() {
        return new SosialhjelpPdfGenerator();
    }

    @Bean
    public TextHelpers textHelpers() {
        return new TextHelpers();
    }

}
