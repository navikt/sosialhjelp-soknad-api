package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

public class InnsendtSoknadDto {

    private final String navn;
    private final String kode;
    private final String sisteEndring;

    public InnsendtSoknadDto(
            String navn,
            String kode,
            String sisteEndring
    ) {
        this.navn = navn;
        this.kode = kode;
        this.sisteEndring = sisteEndring;
    }

    public String getNavn() {
        return navn;
    }

    public String getKode() {
        return kode;
    }

    public String getSisteEndring() {
        return sisteEndring;
    }

}
