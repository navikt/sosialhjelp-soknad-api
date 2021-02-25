package no.nav.sosialhjelp.soknad.consumer.pdl;

import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlEktefelle;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPerson;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapperHelper;
import no.nav.sosialhjelp.soknad.domain.model.Barn;
import no.nav.sosialhjelp.soknad.domain.model.Ektefelle;
import no.nav.sosialhjelp.soknad.domain.model.NavFodselsnummer;
import no.nav.sosialhjelp.soknad.domain.model.Person;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.GIFT;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.REGISTRERT_PARTNER;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlService {

    private static final Logger log = getLogger(PdlService.class);
    private static final String BARN = "BARN";

    private final PdlConsumer pdlConsumer;
    private final PdlPersonMapper pdlPersonMapper;

    private final PdlPersonMapperHelper helper = new PdlPersonMapperHelper();

    public PdlService(PdlConsumer pdlConsumer, PdlPersonMapper pdlPersonMapper) {
        this.pdlConsumer = pdlConsumer;
        this.pdlPersonMapper = pdlPersonMapper;
    }

    public Person hentPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);
        if (pdlPerson == null) {
            return null;
        }

        return pdlPersonMapper.mapToPerson(pdlPerson, ident)
                .withEktefelle(hentEktefelle(pdlPerson));
    }

    public List<Barn> hentBarnForPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);

        if (pdlPerson == null || pdlPerson.getForelderBarnRelasjon() == null) {
            return null;
        }

        List<Barn> alleBarn = pdlPerson.getForelderBarnRelasjon().stream()
                .filter(forelderBarnRelasjonDto -> forelderBarnRelasjonDto.getRelatertPersonsRolle().equalsIgnoreCase(BARN))
                .map(forelderBarnRelasjonDto -> {
                    var barnIdent = forelderBarnRelasjonDto.getRelatertPersonsIdent();

                    if (barnIdent == null || barnIdent.isEmpty()) {
                        log.info("ForelderBarnRelasjon.relatertPersonsIdent (barnIdent) er null -> kaller ikke hentPerson for barn");
                        return null;
                    }
                    if (erFDAT(barnIdent)) {
                        log.info("ForelderBarnRelasjon.relatertPersonsIdent (barnIdent) er FDAT -> kaller ikke hentPerson for barn");
                        return null;
                    }

                    loggHvisIdentIkkeErFnr(barnIdent);
                    var pdlBarn = pdlConsumer.hentBarn(barnIdent);
                    return pdlPersonMapper.mapToBarn(pdlBarn, barnIdent, pdlPerson);
                })
                .collect(Collectors.toList());
        alleBarn.removeIf(Objects::isNull);
        return alleBarn;
    }

    private Ektefelle hentEktefelle(PdlPerson pdlPerson) {
        if (pdlPerson != null && pdlPerson.getSivilstand() != null && !pdlPerson.getSivilstand().isEmpty()) {

            var sivilstand = helper.utledGjeldendeSivilstand(pdlPerson.getSivilstand());
            if (sivilstand != null && (GIFT == sivilstand.getType() || REGISTRERT_PARTNER == sivilstand.getType())) {
                String ektefelleIdent = sivilstand.getRelatertVedSivilstand();

                if (ektefelleIdent == null || ektefelleIdent.isEmpty()) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er null -> kaller ikke hentPerson for ektefelle");
                    return null;
                }
                if (erFDAT(ektefelleIdent)) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er FDAT -> kaller ikke hentPerson for ektefelle");
                    return new Ektefelle()
                            .withFornavn("")
                            .withMellomnavn("")
                            .withEtternavn("")
                            .withFodselsdato(finnFodselsdatoFraFnr(ektefelleIdent))
                            .withFnr(ektefelleIdent)
                            .withFolkeregistrertsammen(false)
                            .withIkketilgangtilektefelle(false);
                }

                loggHvisIdentIkkeErFnr(ektefelleIdent);

                PdlEktefelle pdlEktefelle = pdlConsumer.hentEktefelle(ektefelleIdent);
                return pdlPersonMapper.mapToEktefelle(pdlEktefelle, ektefelleIdent, pdlPerson);
            }
        }
        return null;
    }

    private boolean erFDAT(String ident) {
        return ident.length() == 11 && ident.substring(6).equalsIgnoreCase("00000");
    }

    private LocalDate finnFodselsdatoFraFnr(String ident) {
        NavFodselsnummer fnr = new NavFodselsnummer(ident);
        return new LocalDate(fnr.getBirthYear() + "-" + fnr.getMonth() + "-" + fnr.getDayInMonth());
    }

    private void loggHvisIdentIkkeErFnr(String ektefelleIdent) {
        if (ektefelleIdent.length() == 11 && Integer.parseInt(ektefelleIdent.substring(0, 2)) > 31) {
            log.info("Ident er DNR");
        }
        if (ektefelleIdent.length() == 11 && Integer.parseInt(ektefelleIdent.substring(2, 4)) >= 21 && Integer.parseInt(ektefelleIdent.substring(2, 4)) <= 32) {
            log.info("Ident er NPID");
        }
        if (ektefelleIdent.length() > 11) {
            log.info("Ident er aktørid");
        }
        if (ektefelleIdent.length() < 11) {
            log.info("Ident er ukjent (mindre enn 11 tegn)");
        }
    }

}
