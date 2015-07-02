package no.nav.sbl.dialogarena.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.mockito.Matchers.any;
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
    public void sendEpost() {
        emailService.sendEpost("til", "subject", "innhold", "123");

        final String htmlInnhold = "<p>" + "innhold" + "</p>";

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("til"));
                mimeMessage.setFrom(new InternetAddress("ikke-svar@nav.no"));
                mimeMessage.setContent(htmlInnhold, "text/html;charset=utf-8");
                mimeMessage.setSubject("subject");
            }
        };
        verify(mailSender).send(any(MimeMessagePreparator.class));
    }
}
