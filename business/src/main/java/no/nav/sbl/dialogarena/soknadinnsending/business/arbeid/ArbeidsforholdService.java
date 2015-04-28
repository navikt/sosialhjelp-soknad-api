package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import java.util.List;

public interface ArbeidsforholdService {
    public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer);
    public void lagreArbeidsforhold(String fodselsnummer, Long soknadId);
    }
