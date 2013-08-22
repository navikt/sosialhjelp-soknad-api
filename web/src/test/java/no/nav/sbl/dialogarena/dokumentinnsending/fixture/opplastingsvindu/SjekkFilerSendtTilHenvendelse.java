package no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu;

import no.nav.modig.lang.collections.iter.PreparedIterable;
import no.nav.modig.test.Ignore;
import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.sbl.dialogarena.detect.IsPdf;
import no.nav.sbl.dialogarena.dokumentinnsending.service.HenvendelseIntegrationStub;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;

import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.TransformerUtils.toUpperCase;
import static no.nav.sbl.dialogarena.detect.Functions.CONTENT_SUBTYPE;
import static no.nav.sbl.dialogarena.detect.Functions.CONTENT_TYPE;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils.DOKUMENTINNHOLD;
import static no.nav.sbl.dialogarena.pdf.PdfFunctions.PDF_SIDEANTALL;


public class SjekkFilerSendtTilHenvendelse extends ObjectPerRowFixture<SjekkFilerSendtTilHenvendelse.Oversendt> {

    static class Oversendt {

        @NoCompare
        String brukerbehandlingId;

        int antall;
        List<String> filtyper;
        List<Integer> sideantall;

        @Ignore
        String kommentar;
    }


    private HenvendelseIntegrationStub serviceMock;

    public SjekkFilerSendtTilHenvendelse(HenvendelseIntegrationStub serviceMock) {
        this.serviceMock = serviceMock;
    }

    @Override
    protected void perRow(Row<Oversendt> row) throws Exception {
        PreparedIterable<WSDokument> wsDokumenter = serviceMock.getOpplastedeDokumenterFor(row.expected.brukerbehandlingId);
		List<byte[]> dokumenter = wsDokumenter.map(DOKUMENTINNHOLD).collect();
        row.expected.antall = dokumenter.size();
        row.expected.filtyper = on(dokumenter).map(CONTENT_TYPE).map(CONTENT_SUBTYPE).map(toUpperCase()).collect();
        row.expected.sideantall = on(dokumenter).filter(new IsPdf()).map(PDF_SIDEANTALL).collect();
        
        row.isActually(row.expected);
    }

}
