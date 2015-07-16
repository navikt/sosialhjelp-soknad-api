package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import no.nav.sbl.dialogarena.service.HandlebarRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class RegistryAwareHelper<T> implements Helper<T>{

    @Inject
    private HandlebarRegistry handlebarsRegistry;

    @PostConstruct
    public void registrer(){
        handlebarsRegistry.registrerHelper(getName(), getHelper());
    };

    public abstract String getName();
    public abstract Helper<T> getHelper();

}
