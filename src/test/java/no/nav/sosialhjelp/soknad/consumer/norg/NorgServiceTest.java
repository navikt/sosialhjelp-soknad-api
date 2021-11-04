package no.nav.sosialhjelp.soknad.consumer.norg;

import no.nav.sosialhjelp.soknad.client.norg.NorgClient;
import no.nav.sosialhjelp.soknad.client.norg.dto.NavEnhetDto;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.navenhet.NavEnhet;
import no.nav.sosialhjelp.soknad.domain.model.navenhet.NavenhetFraLokalListe;
import no.nav.sosialhjelp.soknad.domain.model.navenhet.NavenheterFraLokalListe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.System.setProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NorgServiceTest {

    private static final String GT = "0101";
    private static final String ENHETSNUMMER = "0701";
    private static final String ORGNUMMER_PROD = "974605171";
    private static final String ORGNUMMER_TEST = "910940066";

    @Mock
    private NorgClient norgClient;
    @Mock
    private RedisService redisService;
    @InjectMocks
    private NorgService norgService;

    private NavEnhetDto navEnhetDto = new NavEnhetDto("Nav Enhet", ENHETSNUMMER);

    @AfterEach
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    void finnEnhetForGtBrukerTestOrgNrForTest() {
        setProperty("environment.name", "test");
        when(norgClient.hentNavEnhetForGeografiskTilknytning(GT)).thenReturn(navEnhetDto);

        NavEnhet navEnhet = norgService.getEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr).isEqualTo(ORGNUMMER_TEST);
    }

    @Test
    void finnEnhetForGtBrukerOrgNrFraNorgForProd() {
        setProperty("environment.name", "p");
        when(norgClient.hentNavEnhetForGeografiskTilknytning(GT)).thenReturn(navEnhetDto);

        NavEnhet navEnhet = norgService.getEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr).isEqualTo(ORGNUMMER_PROD);
    }

    @Test
    void finnEnhetForLom() {
        setProperty("environment.name", "p");

        String gt = "3434";
        String sosialOrgNummer = "974592274";
        var navEnhetDtoLom = new NavEnhetDto("Nav Enhet", "0513");
        when(norgClient.hentNavEnhetForGeografiskTilknytning(gt)).thenReturn(navEnhetDtoLom);

        NavEnhet navEnhet = norgService.getEnhetForGt(gt);
        assertThat(navEnhet.sosialOrgnr).isEqualTo(sosialOrgNummer);
    }

    @Test
    void finnEnhetForSkjaak() {
        setProperty("environment.name", "p");

        String gt = "3432";
        String sosialOrgNummer = "976641175";
        var navEnhetDtoSjaak = new NavEnhetDto("Nav Enhet", "0513");
        when(norgClient.hentNavEnhetForGeografiskTilknytning(gt)).thenReturn(navEnhetDtoSjaak);

        NavEnhet navEnhet = norgService.getEnhetForGt(gt);
        assertThat(navEnhet.sosialOrgnr).isEqualTo(sosialOrgNummer);
    }

    @Test
    void getEnheterForKommunenummer_forKommunerMedFlereNavenheter_skalReturnereRettAntall() {
        // Oslo
        List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer("0301");
        assertThat(enheterForKommunenummer).hasSize(15);

        // Bergen
        enheterForKommunenummer = norgService.getEnheterForKommunenummer("4601");
        assertThat(enheterForKommunenummer).hasSize(8); // Ikke 5?

        // Stavanger
        enheterForKommunenummer = norgService.getEnheterForKommunenummer("1103");
        assertThat(enheterForKommunenummer).hasSize(5);

        // Trondheim
        enheterForKommunenummer = norgService.getEnheterForKommunenummer("5001");
        assertThat(enheterForKommunenummer).hasSize(2);
    }

    @Test
    void getEnheterForKommunenummer_forKommunerEnNavenhet_skalReturnereEnNavenhet() {
        // Viken
        List<String> kommuner = Arrays.asList("3001", "3002", "3003", "3004", "3005", "3006", "3007", "3011", "3012", "3013", "3014", "3015", "3016", "3017", "3018", "3019", "3020", "3021", "3022", "3023", "3024", "3025", "3026", "3027", "3028", "3029", "3030", "3031", "3032", "3033", "3034", "3035", "3036", "3037", "3038", "3039", "3040", "3041", "3042", "3043", "3044", "3045", "3046", "3047", "3048", "3049", "3050", "3051", "3052", "3053", "3054");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Innlandet
        kommuner = Arrays.asList("3401", "3403", "3405", "3407", "3411", "3412", "3413", "3414", "3415", "3416", "3417", "3418", "3419", "3420", "3421", "3422", "3423", "3424", "3425", "3426", "3427", "3428", "3429", "3430", "3431", "3432", "3433", "3434", "3435", "3436", "3437", "3438", "3439", "3440", "3441", "3442", "3443", "3446", "3447", "3448", "3449", "3450", "3451", "3452", "3453", "3454");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Vestfold og Telemark
        kommuner = Arrays.asList("3801", "3802", "3803", "3804", "3805", "3806", "3807", "3808", "3811", "3812", "3813", "3814", "3815", "3816", "3817", "3818", "3819", "3820", "3821", "3822", "3823", "3824", "3825");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Agder
        kommuner = Arrays.asList("4201", "4202", "4203", "4204", "4205", "4206", "4207", "4211", "4212", "4213", "4214", "4215", "4216", "4217", "4218", "4219", "4220", "4221", "4222", "4223", "4224", "4225", "4226", "4227", "4228");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Rogaland
        kommuner = Arrays.asList("1101", "1106", "1108", "1111", "1112", "1114", "1119", "1120", "1121", "1122", "1124", "1127", "1130", "1133", "1134", "1135", "1144", "1145", "1146", "1149", "1151", "1160");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Vestland
        kommuner = Arrays.asList("4602", "4611", "4612", "4613", "4614", "4615", "4616", "4617", "4618", "4619", "4620", "4621", "4622", "4623", "4624", "4625", "4626", "4627", "4628", "4629", "4630", "4631", "4632", "4633", "4634", "4635", "4636", "4637", "4638", "4639", "4640", "4641", "4642", "4643", "4644", "4645", "4646", "4647", "4648", "4649", "4650", "4651");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Møre og Romsdal
        kommuner = Arrays.asList("1505", "1506", "1507", "1511", "1514", "1515", "1516", "1517", "1520", "1525", "1528", "1531", "1532", "1535", "1539", "1539", "1547", "1554", "1557", "1560", "1563", "1566", "1573", "1573", "1576", "1577", "1578", "1579");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Trøndelag
        kommuner = Arrays.asList("5006", "5007", "5014", "5020", "5021", "5022", "5025", "5026", "5027", "5028", "5029", "5031", "5032", "5033", "5034", "5035", "5036", "5037", "5038", "5041", "5042", "5043", "5044", "5045", "5046", "5047", "5049", "5052", "5053", "5054", "5055", "5056", "5057", "5058", "5059", "5060", "5061");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Nordland
        kommuner = Arrays.asList("1804", "1806", "1812", "1813", "1815", "1816", "1818", "1820", "1822", "1824", "1825", "1826", "1827", "1828", "1832", "1833", "1834", "1835", "1836", "1837", "1838", "1839", "1840", "1841", "1845", "1848", "1851", "1853", "1856", "1857", "1859", "1860", "1865", "1866", "1867", "1868", "1870", "1871", "1874");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });

        // Troms og Finnmark
        kommuner = Arrays.asList("5401", "5402", "5403", "5404", "5405", "5406", "5411", "5412", "5413", "5414", "5415", "5416", "5417", "5418", "5419", "5420", "5421", "5422", "5423", "5424", "5425", "5426", "5427", "5428", "5429", "5430", "5432", "5433", "5434", "5435", "5436", "5437", "5438", "5439", "5440", "5441", "5442", "5443", "5444");
        kommuner.forEach(kommune -> {
            List<NavEnhet> enheterForKommunenummer = norgService.getEnheterForKommunenummer(kommune);
            assertThat(enheterForKommunenummer).hasSize(1);
        });
    }

    @Disabled
    @Test
    void getAllNavenheterFromPath() {
        NavenheterFraLokalListe allNavenheterFromPath = norgService.getAllNavenheterFromPath();
        List<NavenhetFraLokalListe> navenhetnavnUlikKommunenavn = new ArrayList<>();
        List<NavenhetFraLokalListe> kommunenavnMedSpesifisertSted = new ArrayList<>();
        List<NavenhetFraLokalListe> samiskeKommunenavn = new ArrayList<>();
        Map<String, List<NavenhetFraLokalListe>> kommuneMap = new HashMap<>();
        Map<String, List<NavenhetFraLokalListe>> enhetsnavnMap = new HashMap<>();

        for (NavenhetFraLokalListe navenhet : allNavenheterFromPath.navenheter) {
            assertThat(navenhet.kommunenavn).isNotNull();
            assertThat(navenhet.kommunenavn).isNotBlank();
            if (!navenhet.enhetsnavn.contains(navenhet.kommunenavn) &&
                    !navenhet.kommunenavn.equals("Oslo") &&
                    !navenhet.kommunenavn.equals("Bergen") &&
                    !navenhet.kommunenavn.equals("Stavanger") &&
                    !navenhet.kommunenavn.equals("Trondheim")) {

                if (navenhet.kommunenavn.contains(" - ") || navenhet.kommunenavn.contains(" – ")) {
                    samiskeKommunenavn.add(navenhet);
                } else if(navenhet.kommunenavn.contains(" i ")) {
                    kommunenavnMedSpesifisertSted.add(navenhet);
                } else {
                    navenhetnavnUlikKommunenavn.add(navenhet);
                }

                List<NavenhetFraLokalListe> navenheterForEnhetsnavn = enhetsnavnMap.get(navenhet.enhetsnavn);
                if (navenheterForEnhetsnavn == null) navenheterForEnhetsnavn = new ArrayList<>();
                navenheterForEnhetsnavn.add(navenhet);
                enhetsnavnMap.put(navenhet.enhetsnavn, navenheterForEnhetsnavn);
            }
            List<NavenhetFraLokalListe> navenheterForKommune = kommuneMap.get(navenhet.kommunenavn);
            if (navenheterForKommune == null) navenheterForKommune = new ArrayList<>();

            navenheterForKommune.add(navenhet);
            kommuneMap.put(navenhet.kommunenavn, navenheterForKommune);
        }

        Map<String, List<NavenhetFraLokalListe>> kommunerMedFlereNavenheter = kommuneMap.entrySet()
                .stream()
                .filter(stringListEntry -> {
                    return stringListEntry.getValue().size() > 1;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, List<NavenhetFraLokalListe>> enheterForFlerekommuner = enhetsnavnMap.entrySet()
                .stream()
                .filter(stringListEntry -> stringListEntry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        StringBuilder navenheterOverFlereKommuner = new StringBuilder().append("\n~~~~~ Navenheter som strekker seg over flere kommuner: ~~~~~\n");
        for(Map.Entry<String, List<NavenhetFraLokalListe>> entry : enheterForFlerekommuner.entrySet()) {
            String navenhetNavn = entry.getKey();
            List<NavenhetFraLokalListe> navenheter = entry.getValue();
            navenheterOverFlereKommuner.append("\n").append(navenhetNavn).append(":\n");
            navenheter.forEach(navenhet ->
                    navenheterOverFlereKommuner.append("* ").append(navenhet.kommunenavn).append("\n"));
        }
        System.out.println(navenheterOverFlereKommuner);

        StringBuilder samiskeKommuner = new StringBuilder().append("\n~~~~~ Samiske kommuner: ~~~~~\n");
        samiskeKommunenavn.forEach(samiskKommune -> {
            samiskeKommuner.append(samiskKommune.enhetsnavn).append(", ");
            samiskeKommuner.append(samiskKommune.kommunenavn).append(" kommune").append("\n");
        });
        System.out.println(samiskeKommuner);

        StringBuilder spesifisertStedskommuner = new StringBuilder().append("\n~~~~~ Enhetsnavn ulik kommunenavn, der kommunen spesifiserer sted med i : ~~~~~\n");
        kommunenavnMedSpesifisertSted.forEach(kommune -> {
            spesifisertStedskommuner.append(kommune.enhetsnavn).append(", ");
            spesifisertStedskommuner.append(kommune.kommunenavn).append(" kommune").append("\n");
        });
        System.out.println(spesifisertStedskommuner);

        navenhetnavnUlikKommunenavn = navenhetnavnUlikKommunenavn.stream().filter(navenhet -> enheterForFlerekommuner.get(navenhet.enhetsnavn) == null).collect(Collectors.toList()); // filtrere vekk navenheter som strekker seg over flere kommuner
        StringBuilder kommunerNavnUlikNavenhetsnavn = new StringBuilder().append("\n~~~~~ Enhetsnavn ulik kommunenavn: ~~~~~\n");
        navenhetnavnUlikKommunenavn.forEach(kommune -> {
            kommunerNavnUlikNavenhetsnavn.append(kommune.enhetsnavn).append(", ");
            kommunerNavnUlikNavenhetsnavn.append(kommune.kommunenavn).append(" kommune").append("\n");
        });
        System.out.println(kommunerNavnUlikNavenhetsnavn);

        assertThat(allNavenheterFromPath.navenheter.size()).isEqualTo(356 + 17-1 + 8-1 + 9-1 + 4-1); //Totalt antall kommuner + ekstra navkontorer i Oslo, Bergen, Stavanger og Trondheim
    }

    @Test
    void skalHenteNavEnhetForGtFraConsumer() {
        when(norgClient.hentNavEnhetForGeografiskTilknytning(GT)).thenReturn(navEnhetDto);

        NavEnhet navEnhet = norgService.getEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr).isEqualTo(ORGNUMMER_PROD);

        verify(norgClient, times(1)).hentNavEnhetForGeografiskTilknytning(GT);
        verify(redisService, times(1)).getString(anyString());
        verify(redisService, times(0)).get(anyString(), any());
    }

    @Test
    void skalHenteNavEnhetForGtFraCache() {
        when(redisService.getString(anyString())).thenReturn(LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(redisService.get(anyString(), any())).thenReturn(navEnhetDto);

        var navEnhet = norgService.getEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr).isEqualTo(ORGNUMMER_PROD);

        verify(norgClient, times(0)).hentNavEnhetForGeografiskTilknytning(GT);
        verify(redisService, times(1)).getString(anyString());
        verify(redisService, times(1)).get(anyString(), any());
    }

    @Test
    void skalBrukeCacheSomFallbackDersomConsumerFeilerOgCacheFinnes() {
        when(redisService.getString(anyString())).thenReturn(LocalDateTime.now().minusMinutes(60).minusSeconds(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(redisService.get(anyString(), any())).thenReturn(navEnhetDto);
        when(norgClient.hentNavEnhetForGeografiskTilknytning(GT)).thenThrow(new TjenesteUtilgjengeligException("norg feiler", new ServiceUnavailableException()));

        var navEnhet = norgService.getEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr).isEqualTo(ORGNUMMER_PROD);

        verify(norgClient, times(1)).hentNavEnhetForGeografiskTilknytning(GT);
        verify(redisService, times(1)).getString(anyString());
        verify(redisService, times(1)).get(anyString(), any());
    }

    @Test
    void skalKasteFeilHvisConsumerFeilerOgCacheErExpired() {
        when(redisService.getString(anyString())).thenReturn(LocalDateTime.now().minusMinutes(60).minusSeconds(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(redisService.get(anyString(), any())).thenReturn(null);
        when(norgClient.hentNavEnhetForGeografiskTilknytning(GT)).thenThrow(new TjenesteUtilgjengeligException("norg feiler", new ServiceUnavailableException()));

        assertThatExceptionOfType(TjenesteUtilgjengeligException.class).isThrownBy(() -> norgService.getEnhetForGt(GT));

        verify(norgClient, times(1)).hentNavEnhetForGeografiskTilknytning(GT);
        verify(redisService, times(1)).getString(anyString());
        verify(redisService, times(1)).get(anyString(), any());
    }

    @Test
    void skalReturnereNullHvisConsumerReturnererNull() {
        when(redisService.getString(anyString())).thenReturn(LocalDateTime.now().minusMinutes(60).minusSeconds(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(norgClient.hentNavEnhetForGeografiskTilknytning(GT)).thenReturn(null);

        var navEnhet = norgService.getEnhetForGt(GT);

        assertThat(navEnhet).isNull();

        verify(norgClient, times(1)).hentNavEnhetForGeografiskTilknytning(GT);
        verify(redisService, times(1)).getString(anyString());
        verify(redisService, times(0)).get(anyString(), any()); // sjekker ikke cache hvis consumer returnerer null (404 not found)
    }
}