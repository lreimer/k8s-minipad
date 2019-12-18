package dev.ops.tools.k8s;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * The K8s namespace configuration model.
 */
public class K8sNamespace {

    private final String name;
    private final Mode mode;
    private final List<K8sDeployment> deployments;

    public K8sNamespace(String name, Mode mode) {
        this.name = name;
        this.mode = mode;
        this.deployments = new CopyOnWriteArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Mode getMode() {
        return mode;
    }

    public List<K8sDeployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<K8sDeployment> deployments) {
        this.deployments.clear();
        this.deployments.addAll(deployments);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", K8sNamespace.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("mode=" + mode)
                .add("deployments=" + deployments)
                .toString();
    }

    public static List<K8sNamespace> fromJson(JsonArray jsonArray) {
        return jsonArray.getValuesAs((Function<JsonObject, K8sNamespace>) jsonObject -> {
            String name = jsonObject.getString("namespace", "default");
            Mode mode = Mode.valueOf(jsonObject.getString("mode", Mode.LABELS.name()));

            K8sNamespace ns = new K8sNamespace(name, mode);
            if (Mode.MANUAL == mode) {
                List<K8sDeployment> deployments = K8sDeployment.fromJson(jsonObject.getJsonArray("deployments"));
                ns.setDeployments(deployments);
            }

            return ns;
        });
    }

    public enum Mode {
        LABELS, MANUAL;
    }
}
