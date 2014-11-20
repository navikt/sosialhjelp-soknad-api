package no.nav.sbl.dialogarena.websoknad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import javax.inject.Inject;

/**
 * Klasse som tar seg av utsending av epost
 * Alle operasjoner som sender mail er asynkrone. Ingen bekreftelse på om eposten er sendt vil bli gitt.
 */
public class EmailService {

    @Inject
    private MailSender mailSender;
    @Inject
    private TaskExecutor executor;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Sender en epost til innsender med link til skjemaet personen kan fortsette på senre.
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
        mail.setFrom("ikke-svar@nav.no");
        addTask(mail);
    }

    private void addTask(final SimpleMailMessage mail) {
        addTask(mail, 0);
    }

    private void addTask(final SimpleMailMessage mail, final int loopCheck) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mailSender.send(mail);
                    logger.info("Epost sendes: {}", mail);
                } catch (MailException me) {
                    if (loopCheck < 5) {
                        addTask(mail, loopCheck + 1);
                    } else {
                        logger.warn("Kunne ikke sende epost: {}", mail, me);
                    }

                }
            }
        });
    }
}