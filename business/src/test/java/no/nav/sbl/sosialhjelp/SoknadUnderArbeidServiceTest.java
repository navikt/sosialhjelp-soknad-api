package no.nav.sbl.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
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
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadUnderArbeidServiceTest {
    private static final String ORGNR = "012345678";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final String EIER = "12345678910";
    private static final LocalDateTime SIST_ENDRET = now().minusMinutes(5L);
    private static final Long SOKNAD_UNDER_ARBEID_ID = 1L;
    private static final String BEHANDLINGSID = "1100001L";
    private static final String TILKNYTTET_BEHANDLINGSID = "1100002K";
    private static final LocalDateTime OPPRETTET_DATO = now().minusSeconds(50);
    private static final LocalDateTime SIST_ENDRET_DATO = now();

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
    public void settInnsendingstidspunktPaSoknadSkalHandtereEttersendelse() {
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(lagSoknadUnderArbeidForEttersendelse());
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
    public void oppdaterEllerOpprettSoknadUnderArbeidOppretterSoknadHvisDenIkkeFinnes() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(soknadUnderArbeid));

        SoknadUnderArbeid opprettetSoknadUnderArbeidFraDb = soknadUnderArbeidService.oppdaterEllerOpprettSoknadUnderArbeid(soknadUnderArbeid, EIER);

        assertThat(opprettetSoknadUnderArbeidFraDb.getVersjon(), is(1L));
        verify(soknadUnderArbeidRepository, times(1)).opprettSoknad(eq(soknadUnderArbeid), eq(EIER));
    }

    @Test
    public void oppdaterEllerOpprettSoknadUnderArbeidOppdatererSoknadHvisDenFinnes() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString()))
                .thenReturn(Optional.of(soknadUnderArbeid))
                .thenReturn(Optional.of(soknadUnderArbeid.withVersjon(2L)));

        SoknadUnderArbeid oppdatertSoknadUnderArbeidFraDb = soknadUnderArbeidService.oppdaterEllerOpprettSoknadUnderArbeid(soknadUnderArbeid, EIER);

        assertThat(oppdatertSoknadUnderArbeidFraDb.getVersjon(), is(2L));
        verify(soknadUnderArbeidRepository, times(1)).oppdaterSoknadsdata(eq(soknadUnderArbeid), eq(EIER));
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
                .withBehandlingsId(BEHANDLINGSID)
                .withEier(EIER)
                .withVersjon(1L)
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .withData(lagReelleSoknadUnderArbeidData(lagGyldigJsonInternalSoknad()))
                .withSistEndretDato(SIST_ENDRET);
    }

    private SoknadUnderArbeid lagSoknadUnderArbeidForEttersendelse() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private byte[] lagReelleSoknadUnderArbeidData(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
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