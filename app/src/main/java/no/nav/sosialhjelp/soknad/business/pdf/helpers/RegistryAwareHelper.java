package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Helper;
import no.nav.sosialhjelp.soknad.business.pdf.HandlebarRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/*
* Superklasse for å lette implementasjon av helpers til Handlebars.
*
* */

public abstract class RegistryAwareHelper<T> implements Helper<T> {

    @Inject
    private HandlebarRegistry handlebarsRegistry;

    @PostConstruct
    public void registrer() {
        handlebarsRegistry.registrerHelper(getNavn(), this);
    }

    public abstract String getNavn();

    public abstract String getBeskrivelse();
}