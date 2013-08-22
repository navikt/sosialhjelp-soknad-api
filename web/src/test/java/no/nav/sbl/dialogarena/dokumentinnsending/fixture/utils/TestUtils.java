package no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils;

import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.core.context.SubjectHandlerUtils;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.wicket.test.internal.Parameters;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentInnhold;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;

import static no.nav.modig.lang.collections.TransformerUtils.first;
import static org.apache.commons.io.IOUtils.toByteArray;

public final class TestUtils {
	
	static { 
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		System.setProperty(ModigSecurityConstants.SYSTEMUSER_USERNAME, "BD05");
	}

    public static void innloggetBrukerEr(String userId) {
        setSubjectHandler(userId);
    }

    public static void setSubjectHandler(String userId) {
        SubjectHandlerUtils.setEksternBruker(userId, 4, null);
    }

    public static WSDokumentInnhold buildWSDokument() {
        return new WSDokumentInnhold().withFilnavn("Filename")
                .withInnhold(createDataHandler("/testFiles/skjema.pdf"));
    }
    
    public static WSInnsendingsValg mapInnsendingsValg(String innsendingsValg) {
		if (StringUtils.equalsIgnoreCase(innsendingsValg, "sendes ikke")) {
			return WSInnsendingsValg.SENDES_IKKE;
		} else {
			return WSInnsendingsValg.LASTET_OPP;
		}
	}
    
    public static Parameters withBrukerBehandlingId(String behandlingsId) {
        Parameters params = new Parameters();
        params.pageParameters.set("brukerBehandlingId", behandlingsId);
        return params;
    }
    
    public static InnsendingsValg konverterStringTilInnsendingsvalg(String dokumentStatus) {
        if (dokumentStatus.equalsIgnoreCase("Lastet opp")) {
            return InnsendingsValg.LASTET_OPP;
        } else if (dokumentStatus.equalsIgnoreCase("Sendes ikke")) {
        	return InnsendingsValg.SENDES_IKKE;
        } else { 
        	return InnsendingsValg.IKKE_VALGT;
        }
    }

    public static DataHandler createDataHandler(String relativePath) {
        InputStream inputStream = TestUtils.class.getResourceAsStream(relativePath);
        DataHandler handler = null;
        try {
            handler = new DataHandler(new ByteArrayDataSource(inputStream, "application/octet-stream"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return handler;
    }

    public static final Transformer<InputStream, byte[]> READ_STREAM = new Transformer<InputStream, byte[]>() {
        @Override
        public byte[] transform(InputStream stream) {
            try {
                return toByteArray(stream);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    };

    public static final Transformer<WSDokument, Long> DOKUMENT_ID = new Transformer<WSDokument, Long>() {
        @Override
        public Long transform(WSDokument dokument) {
            return dokument.getId();
        }
    };

    public static final Transformer<WSDokumentForventning, Long> FORVENTNINGENS_DOKUMENT = new Transformer<WSDokumentForventning, Long>() {
        @Override
        public Long transform(WSDokumentForventning forventning) {
            return forventning.getDokumentId();
        }
    };

    public static final Transformer<WSDokumentForventning, Long> DOKUMENTFORVENTNING_ID = new Transformer<WSDokumentForventning, Long>() {
        @Override
        public Long transform(WSDokumentForventning forventning) {
            return forventning.getId();
        }
    };

    public static final Transformer<WSDokument, byte[]> DOKUMENTINNHOLD = first(new Transformer<WSDokument, InputStream>() {
        @Override
        public InputStream transform(WSDokument dokument) {
            try {
                return dokument.getInnhold().getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }).then(READ_STREAM);

    public static final Transformer<WSDokument, String> DOKUMENTFILNAVN = new Transformer<WSDokument, String>() {
        @Override
        public String transform(WSDokument dokument) {
            return dokument.getFilnavn();
        }
    };

    private TestUtils() {
    }
}