package io.quarkiverse.zeebe;

import org.jboss.jandex.MethodInfo;

import io.quarkiverse.zeebe.runtime.JobWorkerValue;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.builder.item.MultiBuildItem;

public final class JobWorkerMethodItem extends MultiBuildItem {

    private final BeanInfo bean;
    private final JobWorkerValue worker;
    private final MethodInfo method;
    private final boolean nonBlocking;

    public JobWorkerMethodItem(BeanInfo bean, MethodInfo method, JobWorkerValue worker, String returnType) {
        this(bean, method, worker, false);
    }

    public JobWorkerMethodItem(BeanInfo bean, MethodInfo method, JobWorkerValue worker,
            boolean hasNonBlockingAnnotation) {
        this.bean = bean;
        this.method = method;
        this.worker = worker;
        this.nonBlocking = hasNonBlockingAnnotation
                || ZeebeDotNames.COMPLETION_STAGE.equals(method.returnType().name())
                || ZeebeDotNames.UNI.equals(method.returnType().name());
    }

    public BeanInfo getBean() {
        return bean;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public JobWorkerValue getWorker() {
        return worker;
    }

    public boolean isNonBlocking() {
        return nonBlocking;
    }

}
