package no.nav.sbl.dialogarena.soknadinnsending.business.person;

public interface BolkService {
    String tilbyrBolk();
    void lagreBolk(String fodselsnummer, Long soknadId);

}
