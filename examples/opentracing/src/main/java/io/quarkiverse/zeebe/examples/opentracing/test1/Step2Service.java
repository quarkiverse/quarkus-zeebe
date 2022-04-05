package io.quarkiverse.zeebe.examples.opentracing.test1;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Step2Service {

    static Logger log = LoggerFactory.getLogger(Step2Service.class);

    public String getParam() {
        log.info("Info log");
        return UUID.randomUUID().toString();
    }
}
