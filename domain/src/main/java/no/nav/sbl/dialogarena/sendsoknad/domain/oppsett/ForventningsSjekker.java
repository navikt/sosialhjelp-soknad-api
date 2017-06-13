package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeComparator;
import org.springframework.expression.spel.support.StandardTypeConverter;


public class ForventningsSjekker {
    private static final DefaultConversionService CONVERSION_SERVICE = new DefaultConversionService();
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private static final StandardEvaluationContext CONTEXT = new StandardEvaluationContext();

    static {
        CONTEXT.setTypeConverter(new StandardTypeConverter(CONVERSION_SERVICE));
        CONTEXT.setTypeComparator(new StandardTypeComparator() {
            @Override
            public int compare(Object left, Object right) {
                if (left == null || right == null) {
                    return super.compare(left, right);
                }

                if (left.getClass() == String.class && right.getClass() != String.class) {
                    Class<?> clazz = right.getClass();
                    if (Number.class.isAssignableFrom(right.getClass())) {
                        clazz = Double.class;
                        left = left.toString().replace(",", ".");
                    }

                    return super.compare(CONVERSION_SERVICE.convert(left, clazz), right);
                } else if (right.getClass() == String.class && left.getClass() != String.class) {
                    Class<?> clazz = left.getClass();
                    if (Number.class.isAssignableFrom(left.getClass())) {
                        clazz = Double.class;
                        right = right.toString().replace(",", ".");
                    }
                    return super.compare(left, CONVERSION_SERVICE.convert(right, clazz));
                }
                return super.compare(left, right);
            }
        });
    }

    public static boolean sjekkForventning(String forventning, Faktum value) {
        if (value == null) {
            return false;
        }
        try {
            return PARSER.parseExpression(forventning).getValue(CONTEXT, value, Boolean.class);
        } catch (EvaluationException e) {
            e.printStackTrace();
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
