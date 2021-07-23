package no.nav.sosialhjelp.soknad;

import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return configureApplication(builder);
    }

    public static void main(String[] args) {
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    private static SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {

        if (!ServiceUtils.envIsNonProduction() && MockUtils.isMockAltProfil()) {
            throw new Error("mockAltProfil har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
        }

        if (!ServiceUtils.envIsNonProduction() && MockUtils.isRunningWithInMemoryDb()) {
            throw new Error("no.nav.sosialhjelp.soknad.hsqldb har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
        }

        if (!ServiceUtils.envIsNonProduction() && (MockUtils.isAlltidHentKommuneInfoFraNavTestkommune() || MockUtils.isAlltidSendTilNavTestkommune())) {
            throw new Error("Alltid send eller hent fra NavTestkommune er satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
        }

        return builder
                .sources(Application.class)
                .bannerMode(Banner.Mode.OFF);
    }

}