package no.nav.sbl.dialogarena.utils;

import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;

import javax.inject.Inject;

public class InnloggetBruker {

    @Inject
    private PersonService personService;

    public String hentFornavn() {
        String fnr = OidcFeatureToggleUtils.getUserId();
        Person person = personService.hentPerson(fnr);
        if (person == null){
            return "";
        }
        return person.getFornavn() != null ? person.getFornavn() : "";
    }
}
