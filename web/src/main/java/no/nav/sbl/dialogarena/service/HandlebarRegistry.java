package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Helper;

public interface HandlebarRegistry {

    void registrerHelper(String name, Helper helper);
}
