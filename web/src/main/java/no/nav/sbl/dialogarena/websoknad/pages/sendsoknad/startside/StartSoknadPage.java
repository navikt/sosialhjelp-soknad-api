package no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.startside;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;

public class StartSoknadPage extends BasePage {

	
	public StartSoknadPage(PageParameters pageParameters) {
		StringValue soknadsType = pageParameters.get("soknad");
		if(soknadsType.equals("dagpenger")) {
			System.out.println(soknadsType);
		}else if (soknadsType.equals("barnebidrag")) {
			System.out.println(soknadsType);
		}
		
		
	}
}
