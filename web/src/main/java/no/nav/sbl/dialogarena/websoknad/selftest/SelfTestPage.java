package no.nav.sbl.dialogarena.websoknad.selftest;

import no.nav.modig.wicket.selftest.SelfTestBase;
import no.nav.sbl.dialogarena.dokumentinnsending.config.InternalStsConfig;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import java.util.List;

@Import({InternalStsConfig.class})
public class SelfTestPage extends SelfTestBase {

    @Inject
    private HenvendelsesBehandlingPortType henvendelsesBehandlingPortType;

    public SelfTestPage(PageParameters params) {
        super("sendsoknad", params);
    }

    @Override
    protected void addToStatusList(List<AvhengighetStatus> statusList) {
        new ServiceStatusHenter("HenvendelsesBehandling") {
            public void ping() {
                henvendelsesBehandlingPortType.ping();
            }
        }.addStatus(statusList);
    }
}