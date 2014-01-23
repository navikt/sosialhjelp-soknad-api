package no.nav.sbl.dialogarena.soknadinnsending.business.person;

public interface PersonaliaService {
    Personalia hentPersonalia(String fodselsnummer);

    Personalia lagrePersonaliaOgBarn(String fodselsnummer, Long soknadId);
}
