package dev.ops.tools.k8s;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Root class for the K8s configuration model.
 */
public class K8sConfig {

    private final List<K8sNamespace> namespaces = new CopyOnWriteArrayList<>();

    public List<K8sNamespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<K8sNamespace> namespaces) {
        this.namespaces.clear();
        this.namespaces.addAll(namespaces);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", K8sConfig.class.getSimpleName() + "[", "]")
                .add("namespaces=" + namespaces)
                .toString();
    }

    public static K8sConfig fromFile(File file) {
        K8sConfig config = new K8sConfig();
        try (JsonReader reader = Json.createReader(new FileInputStream(file))) {
            JsonArray jsonArray = reader.readArray();
            config.setNamespaces(K8sNamespace.fromJson(jsonArray));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading JSON config file.", e);
        }
        return config;
    }
}
