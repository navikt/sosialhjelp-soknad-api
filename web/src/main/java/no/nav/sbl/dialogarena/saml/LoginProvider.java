package no.nav.sbl.dialogarena.saml;

import no.nav.common.auth.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public interface LoginProvider {
    Optional<Subject> authenticate(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    Optional<String> redirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}