package no.nav.sbl.dialogarena.websoknad.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.content.ContentRetriever;
import no.nav.modig.content.ValueRetriever;
import no.nav.modig.content.ValuesFromContentWithResourceBundleFallback;
import no.nav.modig.content.enonic.HttpContentRetriever;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentConfigTest {

		private static final String DEFAULT_LOCALE = "nb";
	    private static final String INNHOLDSTEKSTER_NB_NO_LOCAL = "content.innholdstekster";
	    private static final String SBL_WEBKOMPONENTER_NB_NO_LOCAL = "content.sbl-webkomponenter";

	    @Bean
	    public ValueRetriever siteContentRetriever() throws URISyntaxException {
	        Map<String, List<URI>> uris = new HashMap<>();
	        uris.put(DEFAULT_LOCALE, new ArrayList<URI>());
	        //uris.put(DEFAULT_LOCALE, Arrays.asList(new URI(cmsBaseUrl + INNHOLDSTEKSTER_NB_NO_REMOTE), new URI(cmsBaseUrl + SBL_WEBKOMPONENTER_NB_NO_REMOTE),  new URI(cmsBaseUrl + SBL_SKJEMABESKRIVELSER_NB_NO_REMOTE)));

	        return new ValuesFromContentWithResourceBundleFallback(
	                Arrays.asList(INNHOLDSTEKSTER_NB_NO_LOCAL, SBL_WEBKOMPONENTER_NB_NO_LOCAL), enonicContentRetriever(),
	                uris, DEFAULT_LOCALE);
	    }

	    @Bean
	    public ContentRetriever enonicContentRetriever() {
	        return new HttpContentRetriever();
	    }

	    @Bean
	    public CmsContentRetriever cmsContentRetriever() throws URISyntaxException {
	        CmsContentRetriever cmsContentRetriever = new CmsContentRetriever();
	        cmsContentRetriever.setDefaultLocale(DEFAULT_LOCALE);
	        cmsContentRetriever.setTeksterRetriever(siteContentRetriever());
	        cmsContentRetriever.setArtikkelRetriever(siteContentRetriever());
	        return cmsContentRetriever;
	    }
   
}