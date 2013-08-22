package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;


import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.EpostInput;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import org.apache.wicket.Component;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import javax.inject.Inject;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class FortsettSenereFixture extends ObjectPerRowFixture<FortsettSenereFixture.FortsettSenere> {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;

    @Inject
    private DokumentServiceMock dokumentServiceMock;

    @Inject
    private MailSender mailSender;

    static class FortsettSenere {
        @NoCompare
        String brukerbehandlingId;
        String epostadressefeltet;
        @NoCompare
        String adresseVedInnsending;
        String feilmelding;
        String epostSendt;
        String sendtTil;
        @NoCompare
        String kommentar;
    }

    @Override
    protected void perRow(Row<FortsettSenereFixture.FortsettSenere> row) throws Exception {
        dokumentServiceMock.settSecurityContextFor(row.expected.brukerbehandlingId);

        FluentWicketTester<WicketApplication> tester = wicketTester.goTo(FortsettSenerePage.class);

        Component epostForm = tester.get().component(withId("epostForm"));
        EpostInput.EmailModel model = (EpostInput.EmailModel) epostForm.getDefaultModel().getObject();

        FortsettSenere actually = new FortsettSenere();
        actually.epostadressefeltet = model.epost;

        tester.inForm("sendLink:epostForm")
                .write("epost", row.expected.adresseVedInnsending)
                .submitWithAjaxButton(withId("sendEpost"));

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atMost(1)).send(mailCaptor.capture());
        if (mailCaptor.getAllValues().size() > 0) {
            actually.epostSendt = "Ja";
            actually.sendtTil = mailCaptor.getValue().getTo()[0];
            actually.feilmelding = "";
        } else {
            actually.epostSendt = "Nei";
            actually.sendtTil = "";
            actually.feilmelding = tester.get().messagesForComponent(withId("epost"), 0).get(0);
        }
        reset(mailSender);

        row.isActually(actually);
    }
}
