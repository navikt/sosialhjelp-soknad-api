package no.nav.sbl.dialogarena.common;

import no.nav.sbl.dialogarena.types.Pingable;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

public abstract class PingExecutor implements Pingable {

    private final String componentName;

    public PingExecutor(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public Ping ping() {
        try {
            pingOperation();
        } catch (Exception e) {
            return feilet(componentName, e);
        }
        return lyktes(componentName);
    }

    protected abstract void pingOperation();
}