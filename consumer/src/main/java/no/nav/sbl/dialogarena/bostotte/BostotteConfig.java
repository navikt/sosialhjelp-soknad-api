package no.nav.sbl.dialogarena.bostotte;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = Bostotte.class)
public class BostotteConfig {
    @Value("${soknad.bostotte.url}")
    private String uri = "";

    public String getUri() {
        return uri;
    }
}
