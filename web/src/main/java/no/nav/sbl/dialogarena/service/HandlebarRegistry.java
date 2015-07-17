package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Helper;

/*
* Interface som skal registere Handlebars-helpers på en Handlebars-instanse
* */

public interface HandlebarRegistry {

    void registrerHelper(String name, Helper helper);
}
