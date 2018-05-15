package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseConsumer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AdresseService {

    @Inject
    private AdresseConsumer adresseConsumer;

    public void soksoksok(String mittsok) {
        AdresseConsumer.AdressesokRespons adressesokRespons = adresseConsumer.sokAdresse(mittsok);

    }
}
