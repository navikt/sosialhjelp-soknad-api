package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
public class InnsendtSoknadDto {

    private final String navn;
    private final String kode;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final LocalDateTime sisteEndring;

    public InnsendtSoknadDto(
            String navn,
            String kode,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") LocalDateTime sisteEndring
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

    public LocalDateTime getSisteEndring() {
        return sisteEndring;
    }

}
