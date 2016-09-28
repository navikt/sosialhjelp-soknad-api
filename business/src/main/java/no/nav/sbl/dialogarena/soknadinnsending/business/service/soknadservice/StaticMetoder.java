package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.KVITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.getSkjemanummer;

public class StaticMetoder {

    public static String skjemanummer(WebSoknad soknad) {
        return soknad.erDagpengeSoknad() ? getSkjemanummer(soknad) : soknad.getskjemaNummer();
    }

    public static String journalforendeEnhet(WebSoknad soknad) {
        String journalforendeEnhet;

        if (soknad.erDagpengeSoknad()) {
            journalforendeEnhet = getJournalforendeEnhet(soknad);
        } else {
            journalforendeEnhet = soknad.getJournalforendeEnhet();
        }
        return journalforendeEnhet;
    }

    public static final Transformer<WSBehandlingskjedeElement, SoknadInnsendingStatus> STATUS = new Transformer<WSBehandlingskjedeElement, SoknadInnsendingStatus>() {
        public SoknadInnsendingStatus transform(WSBehandlingskjedeElement input) {
            return SoknadInnsendingStatus.valueOf(input.getStatus());
        }
    };

    public static final Transformer<WSBehandlingskjedeElement, String> BEHANDLINGS_ID = new Transformer<WSBehandlingskjedeElement, String>() {
        public String transform(WSBehandlingskjedeElement input) {
            return input.getBehandlingsId();
        }
    };


    public static Predicate<XMLMetadata> kvittering() {
        return new Predicate<XMLMetadata>() {
            @Override
            public boolean evaluate(XMLMetadata xmlMetadata) {
                return xmlMetadata instanceof XMLVedlegg && KVITTERING.equals(((XMLVedlegg) xmlMetadata).getSkjemanummer());
            }
        };
    }

    public static DateTime hentOrginalInnsendtDato(List<WSBehandlingskjedeElement> behandlingskjede, String behandlingsId) {
        return on(behandlingskjede)
                .filter(where(BEHANDLINGS_ID, equalTo(behandlingsId)))
                .head()
                .get()
                .getInnsendtDato();
    }

    public static final Comparator<WSBehandlingskjedeElement> ELDSTE_FORST = new Comparator<WSBehandlingskjedeElement>() {
        @Override
        public int compare(WSBehandlingskjedeElement o1, WSBehandlingskjedeElement o2) {
            return sammenlignBehandlingBasertPaaDato(o1, o2);
        }
    };

    public static final Comparator<WSBehandlingskjedeElement> NYESTE_FORST = new Comparator<WSBehandlingskjedeElement>() {
        @Override
        public int compare(WSBehandlingskjedeElement o1, WSBehandlingskjedeElement o2) {
            return sammenlignBehandlingBasertPaaDato(o2, o1);
        }
    };

    private static int sammenlignBehandlingBasertPaaDato(WSBehandlingskjedeElement forst, WSBehandlingskjedeElement sist) {
        DateTime dato1 = forst.getInnsendtDato();
        DateTime dato2 = sist.getInnsendtDato();

        if (dato1 == null && dato2 == null) {
            return 0;
        } else if (dato1 == null) {
            return 1;
        } else if (dato2 == null) {
            return -1;
        }
        return dato1.compareTo(dato2);
    }
}
