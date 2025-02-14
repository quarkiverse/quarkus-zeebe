package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.search.filter.ProcessInstanceFilter;
import io.camunda.zeebe.client.api.search.query.ProcessInstanceQuery;
import io.camunda.zeebe.client.api.search.response.ProcessInstance;
import io.camunda.zeebe.client.api.search.sort.ProcessInstanceSort;

public class ProcessInstanceQueryImpl
        extends AbstractQuery<ProcessInstance, ProcessInstanceQuery, ProcessInstanceFilter, ProcessInstanceSort>
        implements ProcessInstanceQuery {
}
