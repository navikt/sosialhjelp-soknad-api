package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.KVITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.RUTES_I_BRUT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.getSkjemanummer;

public class StaticMetoder {

    public static String skjemanummer(WebSoknad soknad) {
        return soknad.erDagpengeSoknad() ? getSkjemanummer(soknad) : soknad.getskjemaNummer();
    }

    public static String journalforendeEnhet(WebSoknad soknad) {
        String journalforendeEnhet;

        if (soknad.erDagpengeSoknad()) {
            journalforendeEnhet = RUTES_I_BRUT;
        } else {
            journalforendeEnhet = soknad.getJournalforendeEnhet();
        }
        return journalforendeEnhet;
    }

    public static Predicate<XMLMetadata> IKKE_KVITTERING = xmlMetadata ->
            !(xmlMetadata instanceof XMLVedlegg && KVITTERING.equals(((XMLVedlegg) xmlMetadata).getSkjemanummer()));

    public static DateTime hentOrginalInnsendtDato(List<WSBehandlingskjedeElement> behandlingskjede, String behandlingsId) {
        return behandlingskjede.stream()
                .filter(element-> element.getBehandlingsId().equals(behandlingsId))
                .findFirst()
                .get()
                .getInnsendtDato();
    }

    public static final Comparator<WSBehandlingskjedeElement> ELDSTE_FORST = (o1, o2) -> sammenlignBehandlingBasertPaaDato(o1, o2);

    public static final Comparator<WSBehandlingskjedeElement> NYESTE_FORST = (o1, o2) -> sammenlignBehandlingBasertPaaDato(o2, o1);

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
