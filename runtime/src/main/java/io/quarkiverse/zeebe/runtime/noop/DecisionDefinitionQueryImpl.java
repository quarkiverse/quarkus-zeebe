package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.search.filter.DecisionDefinitionFilter;
import io.camunda.zeebe.client.api.search.query.DecisionDefinitionQuery;
import io.camunda.zeebe.client.api.search.response.DecisionDefinition;
import io.camunda.zeebe.client.api.search.sort.DecisionDefinitionSort;

public class DecisionDefinitionQueryImpl
        extends AbstractQuery<DecisionDefinition, DecisionDefinitionQuery, DecisionDefinitionFilter, DecisionDefinitionSort>
        implements DecisionDefinitionQuery {
}
