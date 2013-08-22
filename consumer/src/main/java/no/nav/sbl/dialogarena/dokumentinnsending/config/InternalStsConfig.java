package no.nav.sbl.dialogarena.dokumentinnsending.config;

import no.nav.modig.security.sts.utility.STSConfigurationUtility;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Klasse for konfigurasjon av sts som brukers av provider-ws modulen for kommunikasjon mellom skjemaveileder og dokumentinnsending
 */
@Configuration
public class InternalStsConfig {
    @Inject
    private OppdatereHenvendelsesBehandlingPortType oppdatereHenvendelsesBehandlingPortType;
    @Inject
    private HenvendelsesBehandlingPortType henvendelsesBehandlingPortType;

    @PostConstruct
    public void setupSts() {
        STSConfigurationUtility.configureStsForSystemUser(ClientProxy.getClient(oppdatereHenvendelsesBehandlingPortType));
        STSConfigurationUtility.configureStsForSystemUser(ClientProxy.getClient(henvendelsesBehandlingPortType));
    }
}