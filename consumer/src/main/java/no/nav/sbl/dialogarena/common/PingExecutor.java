package no.nav.sbl.dialogarena.common;

import no.nav.sbl.dialogarena.types.Pingable;

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
            return Ping.feilet(componentName, e);
        }
        return Ping.lyktes(componentName);
    }

    protected abstract void pingOperation();
}