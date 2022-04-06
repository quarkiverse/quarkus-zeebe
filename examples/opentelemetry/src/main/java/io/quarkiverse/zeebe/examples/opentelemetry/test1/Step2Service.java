package io.quarkiverse.zeebe.examples.opentelemetry.test1;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.extension.annotations.WithSpan;

@ApplicationScoped
public class Step2Service {

    static Logger log = LoggerFactory.getLogger(Step2Service.class);

    @WithSpan
    public String getParam() {
        log.info("Info log");
        return UUID.randomUUID().toString();
    }
}
