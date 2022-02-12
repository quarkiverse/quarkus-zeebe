package io.quarkiverse.zeebe.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

public class ProtoBufUtil {

    public static Map<String, String> mapString(Struct s) {
        Map<String, String> tmp = new HashMap<>();
        s.getFieldsMap().forEach((k, v) -> tmp.put(k, ProtoBufUtil.valueToString(v)));
        return tmp;
    }

    public static String valueToString(Value value) {
        switch (value.getKindCase()) {
            case NULL_VALUE:
                return null;
            case STRING_VALUE:
                return value.getStringValue();
            case NUMBER_VALUE:
            case BOOL_VALUE:
            case STRUCT_VALUE:
            case LIST_VALUE:
            default:
                throw new IllegalArgumentException(String.format("Unsupported protobuf header value %s", value));
        }
    }

    public static Map<String, Object> mapObject(Struct s) {
        Map<String, Object> tmp = new HashMap<>();
        s.getFieldsMap().forEach((k, v) -> tmp.put(k, valueToObject(v)));
        return tmp;
    }

    public static Object valueToObject(Value value) {
        switch (value.getKindCase()) {
            case NULL_VALUE:
                return null;
            case NUMBER_VALUE:
                return value.getNumberValue();
            case STRING_VALUE:
                return value.getStringValue();
            case BOOL_VALUE:
                return value.getBoolValue();
            case STRUCT_VALUE:
                return mapObject(value.getStructValue());
            case LIST_VALUE:
                return maplist(value.getListValue().getValuesList());
            default:
                throw new IllegalArgumentException(String.format("Unsupported protobuf value %s", value));
        }
    }

    public static List<Object> maplist(List<Value> values) {
        List<Object> tmp = new ArrayList<>();
        values.forEach(value -> tmp.add(valueToObject(value)));
        return tmp;
    }
}
