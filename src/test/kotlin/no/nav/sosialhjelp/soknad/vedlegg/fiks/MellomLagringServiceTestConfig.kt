package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.DbTestConfig
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@Import(value = [DbTestConfig::class])
@EnableTransactionManagement
@Profile("test")
class MellomLagringServiceTestConfig {

    private val kommuneInfoServiceMock: KommuneInfoService = mockk()
    private val mellomlagringClientMock: MellomlagringClient = mockk()
    private val virusScannerMock: VirusScanner = mockk()

    @Bean
    @Primary
    fun kommuneInfoService(): KommuneInfoService {
        return kommuneInfoServiceMock
    }

    @Bean
    @Primary
    fun mellomLagringClient(): MellomlagringClient {
        return mellomlagringClientMock
    }

    @Bean
    @Primary
    fun virsusScanner(): VirusScanner {
        return virusScannerMock
    }

    @Bean
    fun soknadUnderArbeidService(
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
        kommuneInfoService: KommuneInfoService,
    ): SoknadUnderArbeidService {
        return SoknadUnderArbeidService(soknadUnderArbeidRepository, kommuneInfoService)
    }

    @Bean
    fun mellomLagringService(
        mellomlagringClient: MellomlagringClient,
        soknadUnderArbeidService: SoknadUnderArbeidService,
        virusScanner: VirusScanner,
    ): MellomlagringService {
        return MellomlagringService(mellomlagringClient, soknadUnderArbeidService, virusScanner)
    }
}
