package no.nav.sbl.dialogarena.service;

import no.nav.modig.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Alle operasjoner som sender mail er asynkrone. Ingen bekreftelse på om eposten er sendt vil bli gitt.
 */
public class EmailService {

    @Inject
    private JavaMailSender mailSender;
    @Inject
    @Named("threadPoolTaskExecutor")
    private TaskExecutor executor;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String FRA_ADRESSE = "ikke-svar@nav.no";

    public void sendEpost(final String ePost, final String subject, final String innhold, String behandlingId) {
        final String htmlInnhold = "<p>" + innhold + "</p>";
        addTask(getMimePreperator(ePost, subject, htmlInnhold), behandlingId, ePost, htmlInnhold, 0);
    }

    private MimeMessagePreparator getMimePreperator(final String epost, final String subject, final String innhold) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) {
                try {
                    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(epost));
                    mimeMessage.setFrom(new InternetAddress(FRA_ADRESSE));
                    mimeMessage.setContent(innhold, "text/html;charset=utf-8");
                    mimeMessage.setSubject(subject);
                } catch (MessagingException e) {
                    logger.error("Kunne ikke opprette e-post", e);
                    throw new ApplicationException("Kunne ikke opprette e-post", e);
                }
            }
        };
        return preparator;
    }

    private void addTask(final MimeMessagePreparator preparator, final String behandlingId, final String tilEpost, final String epostinnhold, final int loopCheck) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Epost sendes til: " + tilEpost + " med innhold: " + epostinnhold + " BrukerbehandlingId: " + behandlingId);
                    mailSender.send(preparator);
                } catch (MailException me) {
                    if (loopCheck < 5) {
                        addTask(preparator, behandlingId, tilEpost, epostinnhold, loopCheck + 1);
                    } else {
                        logger.warn("Epost kunne ikke sendes til: " + tilEpost + " med innhold: " + epostinnhold + " BrukerbehandlingId: " + behandlingId, me);
                    }
                }
            }
        });
    }
}