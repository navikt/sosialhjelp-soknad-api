package no.nav.sbl.dialogarena.sendsoknad.domain.norg;

import java.util.List;

public interface NorgConsumer {

    RsNorgEnhet finnEnhetForGeografiskTilknytning(String geografiskTilknytning);
    
    void ping();

    class RsNorgEnhet {
        public long enhetId;
        public String navn;
        public String enhetNr;
        public int antallRessurser;
        public String status;
        public String orgNivaa;
        public String type;
        public String organisasjonsnummer;
        public String sosialeTjenester;
        public String orgNrTilKommunaltNavKontor;

        public RsNorgEnhet withEnhetId(long enhetId) {
            this.enhetId = enhetId;
            return this;
        }

        public RsNorgEnhet withNavn(String navn) {
            this.navn = navn;
            return this;
        }

        public RsNorgEnhet withEnhetNr(String enhetNr) {
            this.enhetNr = enhetNr;
            return this;
        }

        public RsNorgEnhet withOrgNrTilKommunaltNavKontor(String orgNrTilKommunaltNavKontor) {
            this.orgNrTilKommunaltNavKontor = orgNrTilKommunaltNavKontor;
            return this;
        }
    }

    class RsKontaktinformasjon {
        public Long id;
        public String enhetNr;
        public String telefonnummer;
        public String telefonnummerKommentar;
        public String faksnummer;
        public RsEpost epost;
        public RsAdresse postadresse;
        public RsAdresse besoeksadresse;
        public String spesielleOpplysninger;
        public List<RsPublikumsmottak> publikumsmottak;
    }

    class RsEpost {
        public String adresse;
        public String kommentar;
        public boolean kunIntern;
    }

    class RsAdresse {
        public String type;
        public String postnummer;
        public String poststed;

        public String postboksanlegg;
        public String postboksnummer;

        public String gatenavn;
        public String husnummer;
        public String husbokstav;
        public String adresseTilleggsnavn;
    }

    class RsPublikumsmottak {
        public Long id;
        public RsAdresse besoeksadresse;
        public List<RsAapningstid> aapningstider;
        public String stedsbeskrivelse;
    }

    class RsAapningstid {
        public Long id;
        public String dag;
        public String dato;
        public String fra;
        public String til;
        public String kommentar;
        public boolean stengt;
    }

}
