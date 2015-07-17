package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Helper;

/*
* Interface som brukes av Handlebar-helpers for å registerer seg meg navn og implementasjon.
* Mest vanlige bruksomraade er en spring-bean som extender RegistryAwareHelper som
* bruker denne metoden for å tilgjengeliggjøre helpers for Handlebars senere.
*
* @See no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper
*
* */

public interface HandlebarRegistry {

    void registrerHelper(String name, Helper helper);
}
