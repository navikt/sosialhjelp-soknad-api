package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon.Apningstid;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon.Publikumsmottak;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon.Telefon;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon.Adresse.gateadresse;
import static no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon.Adresse.postboks;

@Service
public class NorgService {

    private static final Logger logger = LoggerFactory.getLogger(NorgService.class);

    @Inject
    private NorgConsumer norgConsumer;

    public NavEnhet finnEnhetForGt(String gt) {
        if (gt == null || !gt.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("GT ikke på gyldig format: " + gt);
        }

        RsNorgEnhet rsNorgEnhet = norgConsumer.finnEnhetForGeografiskTilknytning(gt);
        if (rsNorgEnhet == null) {
            logger.warn("Kunne ikke finne NorgEnhet for gt: " + gt);
            return null;
        }

        NavEnhet enhet = new NavEnhet();
        enhet.enhetNr = rsNorgEnhet.enhetNr;
        enhet.navn = rsNorgEnhet.navn;
        if (rsNorgEnhet.enhetNr.equals("0513")  && gt.equals("0514")){
            /*
            Jira sak 1200

            Lom og Skjåk har samme enhetsnummer. Derfor vil alle søknader bli sendt til Skjåk når vi henter organisajonsnummer basert på enhetNr.
            Dette er en midlertidig fix for å få denne casen til å fungere.
            */
            enhet.sosialOrgnr = "974592274";
        } else if (rsNorgEnhet.enhetNr.equals("0511")  && gt.equals("0512")){
            enhet.sosialOrgnr = "964949204";
        } else {
            enhet.sosialOrgnr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(rsNorgEnhet.enhetNr);
        }


        return enhet;
    }

    public Kontaktinformasjon hentKontaktInformasjon(String enhetNr) {
        if (enhetNr == null || !enhetNr.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Enhetnr ikke på gyldig format: " + enhetNr);
        }

        RsKontaktinformasjon rsKontaktinformasjon = norgConsumer.hentKontaktinformasjonForEnhet(enhetNr);

        Kontaktinformasjon kontakt = new Kontaktinformasjon();
        kontakt.postdresse = tilAdresse(rsKontaktinformasjon.postadresse);
        kontakt.telefon = Telefon.telefon(rsKontaktinformasjon.telefonnummer, rsKontaktinformasjon.telefonnummerKommentar);
        kontakt.publikumsmottak = tilPublikumsMottak(rsKontaktinformasjon.publikumsmottak);

        return kontakt;
    }

    private List<Publikumsmottak> tilPublikumsMottak(List<RsPublikumsmottak> publikumsmottak) {
        if (publikumsmottak == null) {
            return new ArrayList<>();
        }

        return publikumsmottak.stream().map(rsMottak -> {
            Publikumsmottak mottak = new Publikumsmottak();
            mottak.besoksadresse = tilAdresse(rsMottak.besoeksadresse);
            mottak.apningstider = tilApningstider(rsMottak.aapningstider);
            return mottak;
        }).collect(toList());
    }

    private List<Apningstid> tilApningstider(List<RsAapningstid> apningstider) {
        if (apningstider == null) {
            return new ArrayList<>();
        }

        return apningstider.stream().map(rsApning -> {
            Apningstid a = new Apningstid();
            a.dag = rsApning.dag;
            a.fra = rsApning.fra;
            a.til = rsApning.til;
            a.stengt = rsApning.stengt;
            a.kommentar = rsApning.kommentar;
            return a;
        }).collect(toList());
    }

    private Kontaktinformasjon.Adresse tilAdresse(RsAdresse rs) {
        if ("postboksadresse".equals(rs.type)) {
            return postboks(rs.postnummer, rs.poststed, rs.postboksanlegg, rs.postboksnummer);
        } else {
            return gateadresse(rs.postnummer, rs.poststed, rs.gatenavn, rs.husnummer, rs.husbokstav, rs.adresseTilleggsnavn);
        }
    }
}
