package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;


public interface PersonaliaService {
    Personalia hentPersonalia(String fodselsnummer) throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
    Personalia lagrePersonaliaOgBarn(String fodselsnummer, Long soknadId);
}
