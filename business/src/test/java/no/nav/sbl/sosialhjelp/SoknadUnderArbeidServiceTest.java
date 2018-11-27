package no.nav.sbl.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.soknadsosialhjelp.soknad.*;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.*;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SoknadUnderArbeidServiceTest {
    private static final String ORGNR = "012345678";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final String EIER = "12345678910";
    private static final LocalDateTime SIST_ENDRET = now().minusMinutes(5L);

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @InjectMocks
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Test
    public void settOrgnummerOgNavEnhetsnavnPaSoknadOppdatererSoknadenIDatabasen() throws SamtidigOppdateringException {
        soknadUnderArbeidService.settOrgnummerOgNavEnhetsnavnPaSoknad(lagSoknadUnderArbeid(), ORGNR, NAVENHETSNAVN, EIER);

        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(any(SoknadUnderArbeid.class), eq(EIER));
    }

    @Test
    public void hentJsonInternalSoknadFraSoknadUnderArbeidOppretterInternalSoknadForGyldigData() {
        JsonInternalSoknad jsonInternalSoknad = soknadUnderArbeidService.hentJsonInternalSoknadFraSoknadUnderArbeid(lagSoknadUnderArbeid());

        assertThat(jsonInternalSoknad.getSoknad().getVersion(), is("1.0.0"));
        assertThat(jsonInternalSoknad.getSoknad().getDriftsinformasjon(), isEmptyString());
        assertThat(jsonInternalSoknad.getSoknad().getData().getPersonalia(), notNullValue());
    }

    @Test
    public void oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknadSetterMottakerinfo() {
        SoknadUnderArbeid oppdatertSoknadUnderArbeid = soknadUnderArbeidService.oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(lagSoknadUnderArbeid(),
                ORGNR, NAVENHETSNAVN);

        JsonInternalSoknad oppdatertInternalSoknad = soknadUnderArbeidService.hentJsonInternalSoknadFraSoknadUnderArbeid(oppdatertSoknadUnderArbeid);
        assertThat(oppdatertInternalSoknad.getMottaker().getOrganisasjonsnummer(), is(ORGNR));
        assertThat(oppdatertInternalSoknad.getMottaker().getNavEnhetsnavn(), is(NAVENHETSNAVN));
    }

    @Test
    public void mapJsonSoknadInternalTilFilReturnererByteArrayHvisInternalSoknadErGyldig() {
        byte[] data = soknadUnderArbeidService.mapJsonSoknadInternalTilFil(lagGyldigJsonInternalSoknad());

        assertThat(data, notNullValue());
    }

    @Test(expected = RuntimeException.class)
    public void mapJsonSoknadInternalTilFilFeilerHvisInternalSoknadErUgyldig() {
        JsonInternalSoknad ugyldigJsonInternalSoknad = new JsonInternalSoknad()
                .withSoknad(new JsonSoknad())
                .withMottaker(new JsonSoknadsmottaker());

        soknadUnderArbeidService.mapJsonSoknadInternalTilFil(ugyldigJsonInternalSoknad);
    }

    private SoknadUnderArbeid lagSoknadUnderArbeid() {
        return new SoknadUnderArbeid()
                .withData(lagReelleSoknadUnderArbeidData(lagGyldigJsonInternalSoknad()))
                .withSistEndretDato(SIST_ENDRET);
    }

    private byte[] lagReelleSoknadUnderArbeidData(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            final String internalSoknad = writer.writeValueAsString(jsonInternalSoknad);
            ensureValidInternalSoknad(internalSoknad);
            return internalSoknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonInternalSoknad lagGyldigJsonInternalSoknad() {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withVersion("1.0.0")
                        .withKompatibilitet(emptyList())
                        .withDriftsinformasjon("")
                        .withData(new JsonData()
                                .withArbeid(new JsonArbeid())
                                .withBegrunnelse(new JsonBegrunnelse()
                                        .withHvaSokesOm("")
                                        .withHvorforSoke(""))
                                .withBosituasjon(new JsonBosituasjon())
                                .withFamilie(new JsonFamilie()
                                        .withForsorgerplikt(new JsonForsorgerplikt()))
                                .withOkonomi(new JsonOkonomi()
                                        .withOpplysninger(new JsonOkonomiopplysninger())
                                        .withOversikt(new JsonOkonomioversikt()))
                                .withPersonalia(new JsonPersonalia()
                                        .withKontonummer(new JsonKontonummer()
                                                .withKilde(JsonKilde.BRUKER))
                                        .withNavn(new JsonSokernavn()
                                                .withFornavn("Fornavn")
                                                .withMellomnavn("")
                                                .withEtternavn("Etternavn")
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM))
                                        .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                .withVerdi("12345678910")
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)))
                                .withUtdanning(new JsonUtdanning()
                                        .withKilde(JsonKilde.BRUKER))));
    }
}