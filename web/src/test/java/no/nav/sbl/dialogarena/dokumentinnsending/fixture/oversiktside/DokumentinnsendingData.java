package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import static java.util.Arrays.asList;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.blank;
import static no.nav.modig.lang.collections.PredicateUtils.not;


public class DokumentinnsendingData {
    public String fulltNavn;
    public String idnummer;
    public String brukerBehandlingId;
    public String type;
    public boolean hovedskjemaVises;
    public String soknad;
    public String soknadOpplastingsdato;
    public String soknadInfotekst;
    public String soknadStatus;

    public String nAVVedlegg1;
    public String nAVVedlegg2;

    public String nAVVedlegg1Opplastingsdato;

    public String nAVVedlegg1Status;
    public String nAVVedlegg2Status;

    public String nAVVedlegg1KnappForHentVedlegg;
    public String nAVVedlegg2KnappForHentVedlegg;

    public String nAVVedlegg1Infotekst;
    public String nAVVedlegg2Infotekst;

    public String eksterntVedlegg1;
    public String eksterntVedlegg2;
    public String eksterntVedlegg3;
    public String eksterntVedlegg4;
    public String eksterntVedlegg5;
    public String eksterntVedlegg6;
    public String eksterntVedlegg7;
    public String eksterntVedlegg8;
    public String eksterntVedlegg9;
    public String eksterntVedlegg10;
    public String eksterntVedlegg11;
    public String eksterntVedlegg12;

    public String eksterntVedlegg1Opplastingsdato;

    public String eksterntVedlegg1Status;
    public String eksterntVedlegg2Status;
    public String eksterntVedlegg3Status;
    public String eksterntVedlegg4Status;

    public String eksterntVedlegg1KnappForHentVedlegg;
    public String eksterntVedlegg2KnappForHentVedlegg;
    public String eksterntVedlegg3KnappForHentVedlegg;

    public String eksterntVedlegg1Infotekst;
    public String eksterntVedlegg2Infotekst;
    public String eksterntVedlegg3Infotekst;

    public String soknadlink;
    public String soknadLinkTekst;

    public String nAVVedlegg1link;
    public String nAVVedlegg1LinkTekst;
    public String eksterntVedlegg1LinkTekst;

    public String forventetResultat;
    public String kommentar;


    public Iterable<String> getNavVedlegg() {
        return on(asList(
                nAVVedlegg1,
                nAVVedlegg2)).filter(not(blank()));
    }


    public Iterable<String> getEksterneVedlegg() {
        return on(asList(
                eksterntVedlegg1,
                eksterntVedlegg2,
                eksterntVedlegg3,
                eksterntVedlegg4,
                eksterntVedlegg5,
                eksterntVedlegg6,
                eksterntVedlegg7,
                eksterntVedlegg8,
                eksterntVedlegg9,
                eksterntVedlegg10,
                eksterntVedlegg11,
                eksterntVedlegg12)).filter(not(blank()));
    }

}
