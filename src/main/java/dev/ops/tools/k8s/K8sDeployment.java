package dev.ops.tools.k8s;

import javax.json.JsonArray;
import javax.json.JsonString;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * The K8s deployment configuration model.
 */
public class K8sDeployment {

    private final String name;

    public K8sDeployment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<K8sDeployment> fromJson(JsonArray jsonArray) {
        return jsonArray.getValuesAs((Function<JsonString, K8sDeployment>) jsonValue -> new K8sDeployment(jsonValue.getString()));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", K8sDeployment.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .toString();
    }
}
