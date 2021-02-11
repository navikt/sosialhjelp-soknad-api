package no.nav.sbl.dialogarena.selftest;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SelftestConfig {

    @Bean
    public SelftestServlet selftestServlet() {
        return new SelftestServlet();
    }

    @Bean
    public SelftestMeterBinder selftestMeterBinder(SelftestServlet selftestServlet) {
        return new SelftestMeterBinder(selftestServlet);
    }

    static class SelftestMeterBinder implements MeterBinder {

        private final SelftestServlet selftestServlet;

        public SelftestMeterBinder(SelftestServlet selftestServlet) {
            this.selftestServlet = selftestServlet;
        }

        @Override
        public void bindTo(@NonNull MeterRegistry registry) {
            Gauge.builder("selftests_aggregate_result_status", this::getAggregateResult)
                    .description("aggregert status for alle selftester. 0=ok, 1=kritisk feil, 2=ikke-kritisk feil")
                    .register(registry);
        }

        private int getAggregateResult() {
            return selftestServlet.lagSelftest().getAggregateResult();
        }
    }
}
