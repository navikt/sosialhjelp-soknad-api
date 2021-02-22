package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component
public class FormaterDecimalHelper extends RegistryAwareHelper<Double> {

    @Override
    public String getNavn() {
        return "formaterDecimal";
    }

    @Override
    public String getBeskrivelse() {
        return "Formaterer et innsendt tall med et gitt antall decimaler som også sendes inn";
    }

    @Override
    public CharSequence apply(Double tall, Options options) {
        if (tall == null) {
            return "";
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("no"));
        DecimalFormat formater = new DecimalFormat("##.##", symbols);
        int decimals = 2;
        if (options.params.length > 0) {
            decimals = Integer.parseInt(options.param(0).toString());
        }
        formater.setMaximumFractionDigits(decimals);
        formater.setMinimumFractionDigits(decimals);
        return formater.format(tall);
    }
}
