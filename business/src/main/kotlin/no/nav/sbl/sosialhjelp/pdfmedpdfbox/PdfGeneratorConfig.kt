package no.nav.sbl.sosialhjelp.pdfmedpdfbox

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException

@Configuration
open class PdfGeneratorConfig {
    @Bean
    @Throws(IOException::class)
    open fun pdfGenerator(): PdfGenerator {
        return PdfGenerator()
    }

    @Bean
    open fun sosialhjelpPdfGenerator(): SosialhjelpPdfGenerator {
        return SosialhjelpPdfGenerator()
    }

    @Bean
    open fun textHelpers(): TextHelpers {
        return TextHelpers()
    }
}