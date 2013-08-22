package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.lang.Resettable;
import no.nav.modig.lang.collections.iter.PreparedIterable;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.Brukerbehandling;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;


public interface HenvendelseIntegrationStub extends Resettable {

    void stub(KodeverkSkjema kodeverkskjema);

    void stub(Brukerbehandling brukerbehandling);

    long opprettDokumentForventning(WSDokumentForventning forventning, String id);

    WSDokument createDokument(String navn);

    PreparedIterable<WSDokument> getOpplastedeDokumenterFor(String behandlingsId);

}
