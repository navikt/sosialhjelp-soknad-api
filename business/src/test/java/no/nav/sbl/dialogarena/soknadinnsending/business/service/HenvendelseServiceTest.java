package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HenvendelseServiceTest {


    @Test
    public void yo() {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < 10e6; i++) {
            String beh = HenvendelseService.lagBehandlingsId(i);
            if (map.containsKey(beh)) {
                System.out.println(map.get(beh) + " og " + i + " mapper til: " + beh);
            }
        }


    }

}