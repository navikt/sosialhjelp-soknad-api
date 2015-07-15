package no.nav.sbl.dialogarena.rest.ressurser.informasjon;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    @InjectMocks
    TjenesterRessurs ressurs;

    @Test
    public void skalHenteAktiviteter() throws Exception {
        assertThat(ressurs.hentAktiviteter()).hasSize(2);
    }
}