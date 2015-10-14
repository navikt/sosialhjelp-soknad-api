package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeComparator;
import org.springframework.expression.spel.support.StandardTypeConverter;


public class ForventningsSjekker {
    private static final DefaultConversionService conversionService = new DefaultConversionService();
    private static final SpelExpressionParser parser = new SpelExpressionParser();
    private static final StandardEvaluationContext context = new StandardEvaluationContext();

    static {
        context.setTypeConverter(new StandardTypeConverter(conversionService));
        context.setTypeComparator(new StandardTypeComparator() {
            @Override
            public int compare(Object left, Object right) throws SpelEvaluationException {
                if (left.getClass() == String.class && right.getClass() != String.class) {
                    return super.compare(conversionService.convert(left, right.getClass()), right);
                } else if (right.getClass() == String.class && left.getClass() != String.class) {
                    return super.compare(left, conversionService.convert(right, left.getClass()));
                }
                return super.compare(left, right);
            }
        });
    }

    public static boolean sjekkForventning(String forventning, Faktum value) {
        try {
            return parser.parseExpression(forventning).getValue(context, value, Boolean.class);
        } catch (EvaluationException e) {
            return false;
        } catch (ParseException e) {
            return false;
        }
    }
}
