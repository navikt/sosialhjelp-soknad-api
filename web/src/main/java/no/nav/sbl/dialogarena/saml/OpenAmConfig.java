package no.nav.sbl.dialogarena.saml;

import java.util.List;

public class OpenAmConfig {
    public List<String> additionalAttributes;
    public OpenAMEventListener openAMEventListener = new OpenAMEventListener.DefaultOpenAMEventListener();
}