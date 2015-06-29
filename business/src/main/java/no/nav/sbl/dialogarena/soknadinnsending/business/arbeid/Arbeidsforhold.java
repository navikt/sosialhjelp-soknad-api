package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;

public class Arbeidsforhold {
    public String orgnr;
    public String arbridsgiverNavn;
    public Land land;
    public String fom;
    public String tom;
    public Long edagId;
    public Long fastStillingsprosent = 0L;
    public boolean variabelStillingsprosent;
    public boolean harFastStilling;
}
