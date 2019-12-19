package dev.ops.tools.k8s;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Root class for the K8s configuration model.
 */
public class K8sModel {

    private final List<K8sNamespace> namespaces = new CopyOnWriteArrayList<>();

    public List<K8sNamespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<K8sNamespace> namespaces) {
        this.namespaces.clear();
        this.namespaces.addAll(namespaces);
    }

    public K8sNamespace getNamespace(int index) {
        return namespaces.get(index);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", K8sModel.class.getSimpleName() + "[", "]")
                .add("namespaces=" + namespaces)
                .toString();
    }

    public static K8sModel fromFile(File file) {
        K8sModel config = new K8sModel();
        try (JsonReader reader = Json.createReader(new FileInputStream(file))) {
            JsonArray jsonArray = reader.readArray();
            config.setNamespaces(K8sNamespace.fromJson(jsonArray));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading JSON config file.", e);
        }
        return config;
    }

    public K8sNamespace getNamespaceByName(String name) {
        K8sNamespace found = null;
        for (K8sNamespace ns : namespaces) {
            if (Objects.equals(ns.getName(), name)) {
                found = ns;
                break;
            }
        }
        return found;
    }
}
