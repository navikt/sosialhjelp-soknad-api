package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators;

import no.nav.modig.content.CmsContentRetriever;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import javax.inject.Inject;

public class CheckboxValidator implements IValidator<Boolean> {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;
    @Override
    public void validate(IValidatable<Boolean> validatable) {
        if (!validatable.getValue()) {
            error(validatable, cmsContentRetriever.hentTekst("validate.checkbox.ikkeAvhuket"));
        }
    }

    private void error(IValidatable<Boolean> validatable, String errorMessage) {
        ValidationError error = new ValidationError();
        error.setMessage(errorMessage);
        validatable.error(error);
    }
}
