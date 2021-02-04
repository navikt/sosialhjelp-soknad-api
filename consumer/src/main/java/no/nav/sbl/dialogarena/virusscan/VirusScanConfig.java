package no.nav.sbl.dialogarena.virusscan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@ComponentScan(basePackageClasses = VirusScanner.class)
public class VirusScanConfig {

    private static final URI DEFAULT_CLAM_URI = URI.create("http://clamav.nais.svc.nais.local/scan");
    @Value("${soknad.vedlegg.virusscan.enabled}")
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public URI getUri() {
        return DEFAULT_CLAM_URI;
    }
}
