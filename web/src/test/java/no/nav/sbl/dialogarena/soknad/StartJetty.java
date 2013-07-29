package no.nav.sbl.dialogarena.soknad;

import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.soknad.config.SystemProperties;

import java.io.IOException;

import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;
import static no.nav.modig.test.util.FilesAndDirs.WEBAPP_SOURCE;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;

public final class StartJetty {

    public static final int PORT = 8181;

    public static void main(String[] args) throws IOException {
        SystemProperties.setFrom("jetty-env.properties");
        Jetty jetty = usingWar(WEBAPP_SOURCE)
                .at("soknad")
                .sslPort(8444)
                .port(PORT).buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    private StartJetty() { }

}
