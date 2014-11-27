package no.nav.sbl.dialogarena.websoknad.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {
    @InjectMocks
    private EmailService emailService;
    @Mock
    private JavaMailSender mailSender;
    @Spy
    private TaskExecutor taskExecutor = new SyncTaskExecutor();

    @Test
    public void sendMail() {
        emailService.sendFortsettSenereEPost("til", "subject", "innhold");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("til");
        message.setSubject("subject");
        message.setText("innhold");
        message.setFrom("ikke-svar@nav.no");
        verify(mailSender).send(refEq(message));
    }

    @Test
    public void sendEpostEtterInnsendtSoknad() {
        emailService.sendEpostEtterInnsendtSoknad("til", "subject", "innhold", "123");

        final String htmlInnhold = "<p>" + "innhold" + "</p>";

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("til"));
                mimeMessage.setFrom("ikke-svar@nav.no");
                mimeMessage.setContent(htmlInnhold, "text/html;charset=utf-8");
                mimeMessage.setSubject("subject");
            }
        };
        verify(mailSender).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void skalProve5Ganger() {
        doThrow(new MailException("messsage"){}).when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendFortsettSenereEPost("til", "subject", "innhold");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("til");
        message.setSubject("subject");
        message.setText("innhold");
        message.setFrom("ikke-svar@nav.no");
        verify(mailSender, times(6)).send(refEq(message));
    }
}
