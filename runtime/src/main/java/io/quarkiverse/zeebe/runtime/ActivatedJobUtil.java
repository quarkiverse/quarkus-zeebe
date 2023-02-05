package io.quarkiverse.zeebe.runtime;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

@Unremovable
public class ActivatedJobUtil {

    public static Object getVariable(ActivatedJob job, String name, Class<?> clazz) {
        Object value = job.getVariablesAsMap().get(name);
        try {
            if (value != null && !clazz.isInstance(value)) {
                // get default zeebe json mapper
                JsonMapper mapper = Arc.container().instance(JsonMapper.class).get();
                String tmp = mapper.toJson(value);
                return mapper.fromJson(tmp, clazz);
            } else {
                return clazz.cast(value);
            }
        } catch (ClassCastException | IllegalArgumentException ex) {
            throw new RuntimeException(
                    "Cannot assign process variable '" + name + "' to parameter of type '" + clazz + "' when executing job '"
                            + job.getType() + "', invalid type found: " + ex.getMessage());
        }
    }

    public static Object getVariablesAsType(ActivatedJob job, Class<?> clazz) {
        try {
            return job.getVariablesAsType(clazz);
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot assign process variables to type '" + clazz.getName() + "' when executing job '"
                    + job.getType() + "', cause is: " + e.getMessage(), e);
        }
    }

    public static String getCustomHeaders(ActivatedJob job, String name) {
        return job.getCustomHeaders().get(name);
    }
}
