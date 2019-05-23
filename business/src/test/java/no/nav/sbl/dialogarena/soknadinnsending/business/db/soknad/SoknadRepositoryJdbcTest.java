package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class SoknadRepositoryJdbcTest {

    @Test
    public void begrensTilNBytes() {
        String streng = SoknadRepositoryJdbc.begrensTilNBytes("åøæabcd", 5);
        assertThat(streng).hasSize(2);
    }
}