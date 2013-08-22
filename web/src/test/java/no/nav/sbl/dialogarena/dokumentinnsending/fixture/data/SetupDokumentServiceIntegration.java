package no.nav.sbl.dialogarena.dokumentinnsending.fixture.data;

import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient;

import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

public class SetupDokumentServiceIntegration extends ObjectPerRowFixture<Brukerbehandling> {

    private DokumentServiceMock dokumentServiceIntegrationMock;

    public SetupDokumentServiceIntegration(DokumentServiceMock dokumentServiceIntegrationMock) {
        this.dokumentServiceIntegrationMock = dokumentServiceIntegrationMock;
        KodeverkSkjema annet = new KodeverkSkjema();
        annet.setBeskrivelse("Dette er ditt skjema");
        annet.setVedleggsid(KodeverkClient.EKSTRA_VEDLEGG_KODEVERKSID);
        annet.setTittel(KodeverkClient.EKSTRA_VEDLEGG_PREFIX);
        this.dokumentServiceIntegrationMock.stub(annet);
    }

    @Override
    protected void perRow(Row<Brukerbehandling> row) throws Exception {
        dokumentServiceIntegrationMock.stub(row.expected);
    }
}
