package no.nav.sosialhjelp.soknad.client.husbanken

internal class HusbankenClientImplTest {

    /*
    todo - skriv om til kotlin tester med mockwebserver

    @Mock
    private BostotteConfig config;

    @Mock
    private RestOperations operations;

    @InjectMocks
    private BostotteImpl bostotte;

    @Captor
    ArgumentCaptor<RequestEntity<BostotteDto>> captor;

    @Test
    void hentBostotte_testUrl_riktigUrlBlirSendtInnTilRestKallet() {
        // Variabler:
        String configUrl = "http://magicUri";
        BostotteDto bostotteDto = new BostotteDto();
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn(configUrl);
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(bostotteDto);
        verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).startsWith(configUrl);
    }

    @Test
    void hentBostotte_testUrl_urlHarRiktigTilOgFraDato() {
        // Variabler:
        String configUrl = "http://magicUri";
        BostotteDto bostotteDto = new BostotteDto();
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn(configUrl);
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(bostotteDto);
        verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).contains("fra=" + fra);
        assertThat(captor.getValue().getUrl().toString()).contains("til=" + til);
    }

    @Test
    void hentBostotte_testJson_testingJsonTranslation() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("husbanken/husbankenSvar.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

        BostotteDto bostotteDto = objectMapper.readValue(jsonString, BostotteDto.class);

        assertThat(bostotteDto.getSaker()).hasSize(3);
        assertThat(bostotteDto.getSaker().get(0).getVedtak().getType()).isEqualTo("INNVILGET");
        assertThat(bostotteDto.getUtbetalinger()).hasSize(2);
        assertThat(bostotteDto.getUtbetalinger().get(0).getUtbetalingsdato()).isEqualTo(LocalDate.of(2019,7,20));
        assertThat(bostotteDto.getUtbetalinger().get(0).getBelop().doubleValue()).isEqualTo(4300.5);
        assertThat(bostotteDto.getUtbetalinger().get(1).getUtbetalingsdato()).isEqualTo(LocalDate.of(2019,8,20));
        assertThat(bostotteDto.getUtbetalinger().get(1).getBelop().doubleValue()).isEqualTo(4300);
    }

    @Test
    void hentBostotte_testUrl_overlevNullUrl() {
        // Variabler:
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn("uri");
        when(operations.exchange(any(), any(Class.class))).thenThrow(new ResourceAccessException("TestException"));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isNull();
    }

    @Test
    void hentBostotte_testUrl_overlevBadConnection() {
        // Variabler:
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn("uri");
        when(operations.exchange(any(), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isNull();
    }

    @Test
    void hentBostotte_testUrl_overlevBadData() {
        // Variabler:
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn("uri");
        when(operations.exchange(any(), any(Class.class))).thenThrow(new HttpMessageNotReadableException("TestException", mock(HttpInputMessage.class)));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isNull();
    }

    @Test
    void hentBostotte_opprettHusbankenPing() {
        // Testkjøring:
        Pingable pingable = BostotteImpl.opprettHusbankenPing(config, new RestTemplate());
        assertThat(pingable).isNotNull();
        assertThat(pingable.ping()).isNotNull();
    }

     */


}