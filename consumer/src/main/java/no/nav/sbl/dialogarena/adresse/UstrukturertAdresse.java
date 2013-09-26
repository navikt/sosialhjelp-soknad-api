package no.nav.sbl.dialogarena.adresse;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.types.Copyable;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.adresse.Adressetype.UKJENT_ADRESSE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UstrukturertAdresse extends Adresse implements Copyable<UstrukturertAdresse> {

    private List<String> adresselinjer = emptyList();
    private String landkode;

    public UstrukturertAdresse(Adressetype type, String landkode, String ... linjer) {
        this(type, landkode, on(linjer));
    }

    public UstrukturertAdresse(Adressetype type, String landkode, Iterable<String> linjer) {
        super(type);
        this.landkode = landkode;
        adresselinjer = new ArrayList<>();
        for (String linje : linjer) {
            if (linje != null) {
                adresselinjer.add(linje);
            }
        }
    }

    public UstrukturertAdresse(Adressetype type, LocalDate utlopsdato, String landkode, String ... linjer) {
        this(type, utlopsdato, landkode, on(linjer));
    }

    public UstrukturertAdresse(Adressetype type, LocalDate utlopsdato, String landkode, Iterable<String> linjer) {
        this(type, landkode, linjer);
        setUtlopsdato(utlopsdato);
    }

    @Override
    public String getLandkode() {
        return landkode;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }

    @Override
    public List<String> somAdresselinjer(Adressekodeverk kodverk) {
        List<String> linjer = new ArrayList<>(adresselinjer);
        if (isNotBlank(landkode)) {
            linjer.add(kodverk.getLand(landkode));
        }
        return linjer;
    }

    public Optional<String> getAdresselinje(int index) {
        if (index >= 0 && index < adresselinjer.size()) {
            return optional(adresselinjer.get(index));
        }
        return none();
    }

    public void setAdresseLinje(String linje, int index) {
        if (index > adresselinjer.size() - 1) {
            adresselinjer.add(linje);
        } else {
            adresselinjer.set(index, linje);
        }
    }

    @Override
    public UstrukturertAdresse copy() {
        return new UstrukturertAdresse(type, getUtlopsdato(), landkode, adresselinjer);
    }

    private UstrukturertAdresse() {
        super(UKJENT_ADRESSE);
    }

}
