package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.search.filter.DecisionRequirementsFilter;
import io.camunda.zeebe.client.api.search.query.DecisionRequirementsQuery;
import io.camunda.zeebe.client.api.search.response.DecisionRequirements;
import io.camunda.zeebe.client.api.search.sort.DecisionRequirementsSort;

public class DecisionRequirementsQueryImpl extends
        AbstractQuery<DecisionRequirements, DecisionRequirementsQuery, DecisionRequirementsFilter, DecisionRequirementsSort>
        implements DecisionRequirementsQuery {
}
