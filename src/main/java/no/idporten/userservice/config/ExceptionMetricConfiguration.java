package no.idporten.userservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.idporten.metric.constants.MetricCategories;
import no.idporten.metric.constants.MetricDescriptions;
import no.idporten.metric.constants.MetricNames;
import no.idporten.metric.constants.MetricValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionMetricConfiguration {


    @Bean(name = "databaseExceptionCounter")
    public Counter connectionIOExceptionMinidCounter(MeterRegistry meterRegistry) {
        return io.micrometer.core.instrument.Counter
                .builder(MetricNames.APP_EXCEPTION_NAME)
                .tag(MetricCategories.EXTERNAL_SYSTEM, MetricValues.EXTERNAL_SYSTEM_DB)
                .tag(MetricCategories.EXCEPTION_TYPE, MetricValues.EXCEPTION_TYPE_CONNECT)
                .description(MetricDescriptions.APP_EXCEPTION_DATABASE_DESCRIPTION)
                .register(meterRegistry);
    }


}
