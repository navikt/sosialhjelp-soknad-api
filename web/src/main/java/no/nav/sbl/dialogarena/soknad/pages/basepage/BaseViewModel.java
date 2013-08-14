package no.nav.sbl.dialogarena.soknad.pages.basepage;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BaseViewModel implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BaseViewModel.class);
    private String tabTittel;
    private Soknad soknad;

    public BaseViewModel(String tabTittel, Soknad soknad) {
        this.tabTittel = tabTittel;
        this.soknad = soknad;
    }

    public final Soknad getSoknad() {
        return soknad;
    }

    public final String getTabTittel() {
        return tabTittel;
    }

    public final String getTittel() {
        return "Tittel!";
    }

    public Long getSoknadId() {
        return soknad.getSoknadId();
    }

    protected static String getApplicationProperty(String key) {
        return new StringResourceModel(key, null, "").getString();
    }

    protected Faktum getFaktum(String key) {
        if (soknad.getFakta().containsKey(key)) {
            return soknad.getFakta().get(key);
        }
        logger.error("Fant ikke nøkkel {} i søknadsstrukturen", key);
        throw new ApplicationException("Fant ikke nøkkelen i søknadsstrukturen");
    }
}