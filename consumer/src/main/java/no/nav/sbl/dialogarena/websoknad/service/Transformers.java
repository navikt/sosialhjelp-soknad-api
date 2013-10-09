package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSBrukerData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadDataOppsummering;
import org.apache.commons.collections15.Transformer;

public class Transformers {

    public static Transformer<WSBrukerData, Faktum> tilFaktum(final Long soknadId) {
        return new Transformer<WSBrukerData, Faktum>() {
            @Override
            public Faktum transform(WSBrukerData wsBrukerData) {
                Faktum faktum = new Faktum();
                faktum.setSoknadId(soknadId);
                faktum.setKey(wsBrukerData.getNokkel());
                faktum.setValue(wsBrukerData.getVerdi());
                faktum.setType(wsBrukerData.getType());
                return faktum;
            }
        };
    }

    public static final Transformer<WSSoknadDataOppsummering, Long> TIL_SOKNADID = new Transformer<WSSoknadDataOppsummering, Long>() {
        @Override
        public Long transform(WSSoknadDataOppsummering wsSoknadDataOppsummering) {
            return wsSoknadDataOppsummering.getSoknadId();
        }
    };

    public static final Transformer<WSSoknadDataOppsummering, String> TIL_STATUS = new Transformer<WSSoknadDataOppsummering, String>() {
        @Override
        public String transform(WSSoknadDataOppsummering wsSoknadDataOppsummering) {
            return wsSoknadDataOppsummering.getStatus();
        }
    };
}
