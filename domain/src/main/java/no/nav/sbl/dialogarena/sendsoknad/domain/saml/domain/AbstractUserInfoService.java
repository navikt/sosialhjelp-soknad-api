package no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain;

public abstract class AbstractUserInfoService implements UserInfoService {

    @Override
    public abstract UserInfo getUserInfo(String subjectId);

}
