package no.nav.sbl.dialogarena.websoknad.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.String.format;

public class NokkelHenter {
    private static final Logger logger = LoggerFactory.getLogger(NokkelHenter.class);

    public static List<String> hentNokler(String side) {
        List<String> nokler = new ArrayList<>();

        String filnavn = format("/templatekeys/%s.txt", side);
        InputStream ressursStrom = NokkelHenter.class.getResourceAsStream(filnavn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(ressursStrom);
            while (scanner.hasNextLine()) {
                nokler.add(scanner.nextLine());
            }
        } catch (NullPointerException e) {
            logger.error(format("Fant ikke følgende fil: %s", filnavn));
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return nokler;
    }
}
