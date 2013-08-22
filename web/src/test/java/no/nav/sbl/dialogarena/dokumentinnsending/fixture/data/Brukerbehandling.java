package no.nav.sbl.dialogarena.dokumentinnsending.fixture.data;


import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBehandlingsstatus;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;

import java.util.List;

import static java.util.Collections.emptyList;

public class Brukerbehandling {

    public String brukerbehandlingId;
    public WSBehandlingsstatus behandlingsstatus;
    public String soknad;
    public String soknadlink;
    public WSInnsendingsValg soknadStatus;
    public boolean erEttersending;

    public List<String> navVedlegg = emptyList();
    public List<String> navVedlegglinker = emptyList();

    public List<String> statuserNavVedlegg = emptyList();
    public List<String> eksterneVedlegg = emptyList();
    public List<String> eksterneVedlegglinker = emptyList();

    public List<String> statuserEksterneVedlegg = emptyList();
    public List<String> annetVedlegg = emptyList();
    public List<String> annetVedlegglinker = emptyList();

    public List<String> statuserAnnetVedlegg = emptyList();
    public String idnummer;
    public String fulltNavn;

    public String type;

    public String kommentar;

    public String epost;
}
