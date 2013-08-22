package no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu;

import no.nav.modig.test.Ignore;
import no.nav.modig.test.NoCompare;

public class ModalvinduForventning {
    @NoCompare
    public String idnummer;
    @NoCompare
    public String brukerBehandlingId;
    @NoCompare
    public String dokument;


    public String tittel;
    public String ingress;


    @Ignore
    public String kommentar;
}