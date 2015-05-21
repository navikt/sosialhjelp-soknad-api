package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;

import java.util.List;

public interface ArbeidsforholdService {
    public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer);
    public List<Faktum> genererArbeidsforhold(String fodselsnummer, Long soknadId);
    }
