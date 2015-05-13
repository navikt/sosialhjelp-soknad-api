package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import java.util.List;

public interface ArbeidsforholdService {
    List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer);
    void lagreArbeidsforhold(String fodselsnummer, Long soknadId);
    }
