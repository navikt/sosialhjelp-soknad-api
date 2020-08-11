package no.nav.sbl.dialogarena.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OpenAMEventListener {

    Logger LOG = LoggerFactory.getLogger(OpenAMEventListener.class);
    String OPENAM_GENERAL_ERROR = "Could not get user attributes from OpenAM. ";

    default void fetchingUserAttributesFailed(OpenAmResponse openAmResponse) {
        LOG.error("{}: {}", OPENAM_GENERAL_ERROR, openAmResponse);
    }

    default void missingUserAttribute(String attribute) {
        LOG.error("{}: Response did not contain attribute: {}", OPENAM_GENERAL_ERROR, attribute);
    }

    class OpenAmResponse {
        public int status;
        public String phrase;
        public String content;

        public OpenAmResponse withStatus(int status) {
            this.status = status;
            return this;
        }

        public OpenAmResponse withPhrase(String phrase) {
            this.phrase = phrase;
            return this;
        }

        public OpenAmResponse withContent(String content) {
            this.content = content;
            return this;
        }

    }

    class DefaultOpenAMEventListener implements OpenAMEventListener {
    }
}