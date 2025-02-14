package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.search.filter.IncidentFilter;
import io.camunda.zeebe.client.api.search.query.IncidentQuery;
import io.camunda.zeebe.client.api.search.response.Incident;
import io.camunda.zeebe.client.api.search.sort.IncidentSort;

public class IncidentQueryImpl extends AbstractQuery<Incident, IncidentQuery, IncidentFilter, IncidentSort>
        implements IncidentQuery {
}
