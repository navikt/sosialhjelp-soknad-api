package no.nav.sosialhjelp.soknad.oppslag;

import org.springframework.stereotype.Component;

@Component
public class KontonummerService {

    private final OppslagConsumer kontonummerConsumer;

    public KontonummerService(OppslagConsumer kontonummerConsumer) {
        this.kontonummerConsumer = kontonummerConsumer;
    }

    public String getKontonummer(String ident) {
        var kontonummer = kontonummerConsumer.getKontonummer(ident);
        if (kontonummer == null) {
            return null;
        }
        return kontonummer.getKontonummer();
    }
}
