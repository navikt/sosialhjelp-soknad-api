package no.nav.sbl.dialogarena.dokumentinnsending.fixture.regresjon;

import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.modig.wicket.test.internal.Parameters;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;

public class RegresjonsFixture extends SpringAwareDoFixture {
    static Parameters withBrukerBehandlingId(String behandlingsId) {
        Parameters params = new Parameters();
        params.pageParameters.set("brukerBehandlingId", behandlingsId);
        return params;
    }

    static InnsendingsValg finnInnsendingsvalgFraDokumentStatus(String soknadDokumentStatus) {
        if (soknadDokumentStatus.equalsIgnoreCase("Lastet opp")) {
            return InnsendingsValg.LASTET_OPP;
        }

        return InnsendingsValg.SEND_SENERE;
    }

    static WSInnsendingsValg finnWSInnsendingsvalgFraDokumentStatus(String soknadDokumentStatus) {
        if (soknadDokumentStatus.equalsIgnoreCase("Lastet opp")) {
            return WSInnsendingsValg.LASTET_OPP;
        }

        return WSInnsendingsValg.SEND_SENERE;
    }

    static KodeverkSkjema lagKodeverkSkjema(String tittel, String skjemanummer) {
        KodeverkSkjema kodeverkSoknadSkjema = new KodeverkSkjema();
        kodeverkSoknadSkjema.setSkjemanummer(skjemanummer);
        kodeverkSoknadSkjema.setTittel(tittel);
        kodeverkSoknadSkjema.setBeskrivelse("252437");
        return kodeverkSoknadSkjema;
    }

    static boolean sjekkDokumentVerdier(Dokument dokument, String tittel, String status, String kodeverkId) {
        boolean riktigKodeverkIdDokument = dokument.getKodeverkId().equalsIgnoreCase(kodeverkId);
        boolean riktigNavnSkjemaDokument = dokument.getNavn().equalsIgnoreCase(tittel);
        boolean riktigStatusDokument = dokument.getValg().equals(finnInnsendingsvalgFraDokumentStatus(status));

        return riktigKodeverkIdDokument && riktigNavnSkjemaDokument && riktigStatusDokument;
    }
}