package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import no.nav.sbl.dialogarena.service.HandleBarKjoerer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class RegistryAwareHelper<T> implements Helper<T>{

    @Inject
    HandleBarKjoerer handleBarKjoerer;

    @PostConstruct
    public void registrer(){
        handleBarKjoerer.registrerHelper(getName(), getHelper());
    };

    public abstract String getName();
    public abstract Helper<T> getHelper();

}
