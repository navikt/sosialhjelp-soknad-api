package no.nav.sbl.dialogarena.dokumentinnsending.kodeverk;

import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

public class KodeverkSkjemaBuilder {

    public String tittel;
    public String link;
    public String beskrivelse;
    public String skjemanummer;

    public KodeverkSkjemaBuilder navn(String tittel) {
        this.tittel = tittel;
        return this;
    }

    public KodeverkSkjemaBuilder link(String link) {
        this.link = link;
        return this;
    }

    public KodeverkSkjemaBuilder beskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public KodeverkSkjemaBuilder kodeverkId(String skjemanummer) {
        this.skjemanummer = skjemanummer;
        return this;
    }

    public KodeverkSkjema build() {
        KodeverkSkjema kodeverkSkjema = new KodeverkSkjema();
        kodeverkSkjema.setTittel(this.tittel);
        kodeverkSkjema.setUrl(this.link);
        kodeverkSkjema.setBeskrivelse(this.beskrivelse);
        kodeverkSkjema.setSkjemanummer(this.skjemanummer);
        return kodeverkSkjema;
    }
}