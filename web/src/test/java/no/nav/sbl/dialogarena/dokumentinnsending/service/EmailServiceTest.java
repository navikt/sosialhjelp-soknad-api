package no.nav.sbl.dialogarena.dokumentinnsending.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    private EmailService service;

    @Mock
    private MailSender mailSender;
    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailMessage;

    @Before
    public void setup() {
        service = new EmailService();
        ReflectionTestUtils.setField(service, "mailSender", mailSender);
        ReflectionTestUtils.setField(service, "executor", new SyncTaskExecutor());
    }

    @Test
    public void skalSendeMailMedSubjectOgInnholdSatt() {
        service.sendFortsettSenereEPost("test@test.com", "mitt subject", "mitt innhold");
        verify(mailSender).send(mailMessage.capture());
        SimpleMailMessage mail = mailMessage.getValue();
        assertThat(mail.getTo()[0], is(equalTo("test@test.com")));
        assertThat(mail.getFrom(), is(equalTo("no-reply@nav.no")));
        assertThat(mail.getSubject(), is(equalTo("mitt subject")));
        assertThat(mail.getText(), is(equalTo("mitt innhold")));
    }
}
