package no.nav.sbl.dialogarena.utils;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;

import javax.inject.Inject;

public class InnloggetBruker {

    @Inject
    private PersonaliaFletter personaliaFletter;

    public Personalia hentPersonalia() {
        String fnr = OidcFeatureToggleUtils.getUserId();
        Personalia personalia = personaliaFletter.mapTilPersonalia(fnr);
        if (personalia == null){
            return new Personalia();
        }
        Personalia kunFornavnPersonalia = new Personalia();
        kunFornavnPersonalia.setFornavn(personalia.getFornavn() != null ? personalia.getFornavn() : "");
        return kunFornavnPersonalia;
    }

    public String hentFornavn() {
        String fnr = OidcFeatureToggleUtils.getUserId();
        Personalia personalia = personaliaFletter.mapTilPersonalia(fnr);
        if (personalia == null){
            return "";
        }
        return personalia.getFornavn() != null ? personalia.getFornavn() : "";
    }
}
