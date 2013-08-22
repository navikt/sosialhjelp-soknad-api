package no.nav.sbl.dialogarena.dokumentinnsending.fixture.data;

import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceMock;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;

public class SetupBrukerprofilIntegration  extends ObjectPerRowFixture<PersonProfil> {

	private PersonServiceMock personService;
	private DokumentServiceMock dokumentServiceMock;
	
	public SetupBrukerprofilIntegration(PersonServiceMock personService, DokumentServiceMock dokumentService) {
		this.personService = personService;
		this.dokumentServiceMock = dokumentService;
	}
	
	@Override
	protected void perRow(Row<PersonProfil> rad) throws Exception {
		stub(rad.expected);
	}
	
	public void stub(PersonProfil personProfil) throws Exception {
		personService.stub(personProfil);
		dokumentServiceMock.stub(createBrukerBehandling());
	}

	private Brukerbehandling createBrukerBehandling() {
		Brukerbehandling brukerBehandling = new Brukerbehandling();
		brukerBehandling.brukerbehandlingId = "11";
		brukerBehandling.type = "arbeidsavklaringspenger";
		brukerBehandling.soknad = "Søknad om arbeidsavklaringspenger";
		brukerBehandling.soknadStatus = WSInnsendingsValg.LASTET_OPP;
		return brukerBehandling;
	}

}
