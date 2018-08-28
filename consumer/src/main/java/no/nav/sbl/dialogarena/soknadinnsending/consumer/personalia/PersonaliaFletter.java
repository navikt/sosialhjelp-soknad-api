package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.PersonaliaBuilder;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.BrukerprofilService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonaliaFletter {

    private static final Logger logger = getLogger(PersonaliaFletter.class);

    @Inject
    private PersonService personService;

    @Inject
    private BrukerprofilService brukerprofilService;

    @Inject
    private EpostService epostService;

    public Personalia mapTilPersonalia(String fodselsnummer) {
        AdresserOgKontonummer adresserOgKontonummer;
        Person person;
        try {
            person = personService.hentPerson(fodselsnummer);
            adresserOgKontonummer = brukerprofilService.hentKontaktinformasjonOgPreferanser(fodselsnummer);
        } catch (IkkeFunnetException e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new ApplicationException("TPS:PersonIkkefunnet", e);
        } catch (SikkerhetsBegrensningException e) {
            logger.warn("Kunne ikke hente bruker fra TPS.", e);
            throw new ApplicationException("TPS:Sikkerhetsbegrensing", e);
        } catch (TjenesteUtilgjengeligException e) {
            logger.error("Ingen kontakt med TPS.", e);
            throw new ApplicationException("TPS:webserviceException", e);
        }
        if (adresserOgKontonummer == null) {
            return new Personalia();
        }

        DigitalKontaktinfo digitalKontaktinfo = epostService.hentInfoFraDKIF(fodselsnummer);

        return PersonaliaBuilder.
                with()
                .fodselsnummer(person.getFnr())
                .fodselsdato(person.getFodselsdato())
                .alder(person.getAlder())
                .diskresjonskode(person.getDiskresjonskode())
                .navn(person.getSammensattNavn())
                .withFornavn(person.getFornavn())
                .withMellomnavn(person.getMellomnavn())
                .withEtternavn(person.getEtternavn())
                .statsborgerskap(person.getStatsborgerskap())
                .kjonn(person.getKjonn())
                .sivilstatus(person.getSivilstatus())
                .ektefelle(personService.hentEktefelle(fodselsnummer))
                .epost(digitalKontaktinfo.getEpostadresse())
                .mobiltelefon(digitalKontaktinfo.getMobilnummer())
                .gjeldendeAdresse(adresserOgKontonummer.getGjeldendeAdresse())
                .sekundarAdresse(adresserOgKontonummer.getSekundarAdresse())
                .folkeregistrertAdresse(adresserOgKontonummer.getFolkeregistrertAdresse())
                .kontonummer(adresserOgKontonummer.getKontonummer())
                .erUtenlandskBankkonto(adresserOgKontonummer.erUtenlandskBankkonto())
                .utenlandskKontoBanknavn(adresserOgKontonummer.getUtenlandskKontoBanknavn())
                .utenlandskKontoLand(adresserOgKontonummer.getUtenlandskKontoLand())
                .build();
    }
}
