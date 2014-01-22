package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class StartSoknadPage extends BasePage {

	public StartSoknadPage(PageParameters parameters) {
		super(parameters);
		
		String soknadType = parameters.get("soknadType").toString();
		
		if(soknadType == null || soknadType.isEmpty()) {
		    add(new SoknadComponent("soknad"));
		} else {
		    add(new SoknadComponent("soknad", soknadType));
		}
		
		
	}
}
