package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;

import javax.xml.ws.WebServiceException;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;

public interface PersonaliaService {
    Personalia hentPersonalia(String fodselsnummer) throws IkkeFunnetException, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, WebServiceException;

    Personalia lagrePersonaliaOgBarn(String fodselsnummer, Long soknadId);
}
