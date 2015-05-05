package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import java.util.List;

public interface SoknadConfig {
    public String getSoknadTypePrefix();
    public String getSoknadUrl();
    public String getFortsettSoknadUrl();
    public List<String> getSoknadBolker();
    public String hentStruktur ();
    public List<String> getSkjemanummer();
}
