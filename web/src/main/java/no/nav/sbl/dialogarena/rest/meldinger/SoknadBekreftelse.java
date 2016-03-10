package no.nav.sbl.dialogarena.rest.meldinger;

import java.io.Serializable;

public class SoknadBekreftelse implements Serializable {
    private String epost;
    private String temaKode;

    private Boolean erEttersendelse;
    private boolean erSoknadsdialog;

    public SoknadBekreftelse() {
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public String getTemaKode() {
        return temaKode;
    }

    public void setTemaKode(String temaKode) {
        this.temaKode = temaKode;
    }

    public Boolean getErEttersendelse() {
        return erEttersendelse;
    }

    public void setErEttersendelse(Boolean erEttersendelse) {
        this.erEttersendelse = erEttersendelse;
    }

    public boolean isErSoknadsdialog() {
        return erSoknadsdialog;
    }

    public void setErSoknadsdialog(boolean erSoknadsdialog) {
        this.erSoknadsdialog = erSoknadsdialog;
    }
}
