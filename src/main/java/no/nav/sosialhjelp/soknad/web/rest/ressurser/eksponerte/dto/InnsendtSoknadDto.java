package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

public class InnsendtSoknadDto {

    private final String navn;
    private final String kode;
    private final String sistEndret;

    public InnsendtSoknadDto(
            String navn,
            String kode,
            String sistEndret
    ) {
        this.navn = navn;
        this.kode = kode;
        this.sistEndret = sistEndret;
    }

    public String getNavn() {
        return navn;
    }

    public String getKode() {
        return kode;
    }

    public String getSistEndret() {
        return sistEndret;
    }

}
