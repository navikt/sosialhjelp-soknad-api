package no.nav.sbl.dialogarena.soknad.domain;

import org.joda.time.DateTime;

import java.io.Serializable;

public class Faktum implements Serializable {

    public Long soknadId;
    public String key;
    public String value;
    public DateTime sistEndret;
}
