package no.nav.sbl.dialogarena.dokumentinnsending.selftest;

import no.nav.modig.wicket.selftest.SelfTestBase;
import no.nav.sbl.dialogarena.dokumentinnsending.config.InternalStsConfig;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import java.util.List;

@Import({InternalStsConfig.class})
public class SelfTestPage extends SelfTestBase {
    @Inject
    private OppdatereHenvendelsesBehandlingPortType oppdatereHenvendelsesBehandlingPortType;

    @Inject
    private HenvendelsesBehandlingPortType henvendelsesBehandlingPortType;

    /*@Inject
    private PersonService personServiceTPS;*/

    public SelfTestPage(PageParameters params) {
        super("dokumentinnsending", params);
    }

    @Override
    protected void addToStatusList(List<AvhengighetStatus> statusList) {
        new ServiceStatusHenter("HenvendelsesBehandling") {
            public void ping() {
                henvendelsesBehandlingPortType.ping();
            }
        }.addStatus(statusList);

         /*new ServiceStatusHenter("PersonService") {
            public void ping() {
                personServiceTPS.ping();
            }
        }.addStatus(statusList);*/
    }
}