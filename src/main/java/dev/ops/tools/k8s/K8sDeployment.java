package dev.ops.tools.k8s;

import io.fabric8.kubernetes.api.model.Pod;

import javax.json.JsonArray;
import javax.json.JsonString;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * The K8s deployment configuration model.
 */
public class K8sDeployment {

    private final String name;
    private final Map<String, String> pods = new TreeMap<>();

    public K8sDeployment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getPods() {
        return pods;
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

    public void setPodList(List<Pod> items) {
        pods.clear();
        for (Pod pod : items) {
            boolean isTerminating = pod.getMetadata().getDeletionTimestamp() != null;
            if (!isTerminating) {
                pods.put(pod.getMetadata().getName(), pod.getStatus().getPhase());
            }
        }
    }
}
