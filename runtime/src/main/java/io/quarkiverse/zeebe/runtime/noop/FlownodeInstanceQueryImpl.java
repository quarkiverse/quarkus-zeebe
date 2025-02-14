package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.search.filter.FlownodeInstanceFilter;
import io.camunda.zeebe.client.api.search.query.FlownodeInstanceQuery;
import io.camunda.zeebe.client.api.search.response.FlowNodeInstance;
import io.camunda.zeebe.client.api.search.sort.FlownodeInstanceSort;

public class FlownodeInstanceQueryImpl
        extends AbstractQuery<FlowNodeInstance, FlownodeInstanceQuery, FlownodeInstanceFilter, FlownodeInstanceSort>
        implements FlownodeInstanceQuery {
}
