package no.nav.sbl.dialogarena.soknad.common;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.pages.soknad.SoknadPage;
import org.apache.wicket.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public enum PageEnum {
    TEST_SOKNAD(SoknadPage.class, "1");

    private static final Logger LOGGER = LoggerFactory.getLogger(PageEnum.class);
    private final String navSoknadId;
    private final Class<? extends BasePage> pageClass;

    PageEnum(Class<? extends BasePage> pageClass, String navSoknadId) {
        this.pageClass = pageClass;
        this.navSoknadId = navSoknadId;
    }

    public boolean erSide(String side) {
        return navSoknadId.equals(side);
    }

    public static Page getPage(Soknad soknad) {
        if (isBlank(soknad.getGosysId())) {
            LOGGER.error("Kan ikke åpne side med tom søknads-ID");
            throw new ApplicationException("Kan ikke åpne side med tom søknads-ID");
        }

        for (PageEnum page : values()) {
            if (page.erSide(soknad.getGosysId())) {
                try {
                    return page.pageClass.getConstructor(Soknad.class).newInstance(soknad);
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    LOGGER.error("Kunne ikke opprette ny side");
                    throw new ApplicationException("Kunne ikke åpne søknad", e);
                }
            }
        }

        LOGGER.error("Fant ikke side knyttet til søknad med ID {}", soknad.getGosysId());
        throw new ApplicationException("Kunne ikke åpne søknaden");
    }
}