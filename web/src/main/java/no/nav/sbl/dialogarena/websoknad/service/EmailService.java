package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.modig.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Klasse som tar seg av utsending av epost
 * Alle operasjoner som sender mail er asynkrone. Ingen bekreftelse på om eposten er sendt vil bli gitt.
 */
public class EmailService {

    @Inject
    private JavaMailSender mailSender;
    @Inject
    private TaskExecutor executor;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final String fraAdresse = "ikke-svar@nav.no";

    /**
     * Sender en epost til innsender med link til skjemaet personen kan fortsette på senere.
     *
     * @param ePost   adressen til personen
     * @param subject Sunbject til mailen
     * @param innhold innhold i mail
     */
    public void sendFortsettSenereEPost(String ePost, String subject, String innhold) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(ePost);
        mail.setSubject(subject);
        mail.setText(innhold);
        mail.setFrom(fraAdresse);
        addTask(mail, "viser ikke behandlingsid for fortsettsenereepost");
    }

    /**
     * Sender en epost til innsender med link til ettersending og saksoversikt.
     *
     * @param ePost        adressen til personen
     * @param subject      Sunbject til mailen
     * @param innhold      innhold i mail
     * @param behandlingId behandinglsiden til søknaden
     */
    public void sendEpostEtterInnsendtSoknad(final String ePost, final String subject, final String innhold, String behandlingId) {
        final String htmlInnhold = "<p>" + innhold + "</p>";
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) {
                try {
                    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(ePost));
                    mimeMessage.setFrom(new InternetAddress(fraAdresse));
                    mimeMessage.setContent(htmlInnhold, "text/html;charset=utf-8");
                    mimeMessage.setSubject(subject);
                } catch (MessagingException e) {
                    logger.error("Kunne ikke opprette e-post", e);
                    throw new ApplicationException("Kunne ikke opprette e-post", e);
                }
            }
        };
        addTask(preparator, behandlingId, ePost, htmlInnhold, 0);
    }

    private void addTask(final SimpleMailMessage mail, final String behandlingId) {
        addTask(mail, behandlingId, 0);
    }

    private void addTask(final SimpleMailMessage mail, final String behandlingId, final int loopCheck) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mailSender.send(mail);
                    logger.info("Epost sendes:" + mail + " BrukerbehandlingId: " + behandlingId);
                } catch (MailException me) {
                    if (loopCheck < 5) {
                        addTask(mail, behandlingId, loopCheck + 1);
                    } else {
                        logger.warn("Kunne ikke sende epost:" + mail + "BrukerbehandlingId: " + behandlingId, me);
                    }
                }
            }
        });
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