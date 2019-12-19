package dev.ops.tools.k8s;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for the Kubernetes related actions and logic. Responsible to
 * control the K8s model state.
 */
public class K8sController implements Watcher<Deployment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sController.class);
    private static final String MINIPAD_ENABLED_LABEL = "k8s-minipad/enabled";

    private final KubernetesClient client;
    private final K8sModel k8sModel;
    private Consumer<String> eventConsumer;

    public K8sController(KubernetesClient client, File configFile) {
        this.client = client;
        this.k8sModel = K8sModel.fromFile(configFile);
    }

    public void initialize() {
        for (K8sNamespace namespace : k8sModel.getNamespaces()) {
            initializeNamespace(namespace);
            client.apps().deployments().inNamespace(namespace.getName()).watch(this);
        }
    }

    private void initializeNamespace(K8sNamespace namespace) {
        if (namespace.getMode() == K8sNamespace.Mode.STATIC) {
            initializeStaticDeployments(namespace);
        }
    }

    private void initializeStaticDeployments(K8sNamespace namespace) {
        List<K8sDeployment> deployments = namespace.getDeployments();
        for (K8sDeployment k8sDeployment : deployments) {
            String deploymentName = k8sDeployment.getName();
            LOGGER.info("Initializing static Deployment {}", deploymentName);

            Deployment deployment = client.apps().deployments().inNamespace(namespace.getName()).withName(deploymentName).get();
            PodList podList = client.pods().inNamespace(namespace.getName()).withLabelSelector(deployment.getSpec().getSelector()).list();
            k8sDeployment.setPodList(podList.getItems());
        }
    }

    private void initializeDeployments(K8sNamespace namespace) {
        DeploymentList list = client.apps().deployments().inNamespace(namespace.getName()).list();
        for (Deployment deployment : list.getItems()) {
            addDeployment(namespace, deployment);
        }
    }

    private void addDeployment(K8sNamespace namespace, Deployment deployment) {
        Map<String, String> labels = Optional.ofNullable(deployment.getMetadata().getLabels()).orElse(Collections.emptyMap());
        boolean enabled = Boolean.parseBoolean(labels.getOrDefault(MINIPAD_ENABLED_LABEL, Boolean.FALSE.toString()));
        if (!enabled) {
            LOGGER.debug("Skipping Deployment {}, not minipad/enabled.", deployment.getMetadata().getName());
        }

        PodList podList = client.pods().inNamespace(namespace.getName()).withLabelSelector(deployment.getSpec().getSelector()).list();

        K8sDeployment k8sDeployment = new K8sDeployment(deployment.getMetadata().getName());
        k8sDeployment.setPodList(podList.getItems());

        LOGGER.info("Adding Deployment {}", k8sDeployment.getName());
        namespace.addDeployment(k8sDeployment);
    }

    private void modifyDeployment(K8sNamespace namespace, Deployment deployment) {
        PodList podList = client.pods().inNamespace(namespace.getName()).withLabelSelector(deployment.getSpec().getSelector()).list();

        K8sDeployment k8sDeployment = namespace.getDeploymentByName(deployment.getMetadata().getName());

        LOGGER.info("Modify Deployment {}", k8sDeployment.getName());
        k8sDeployment.setPodList(podList.getItems());
    }

    private void removeDeployment(K8sNamespace namespace, Deployment deployment) {
        String name = deployment.getMetadata().getName();
        LOGGER.info("Removing Deployment {}", name);
        namespace.removeDeployment(name);
    }

    public K8sModel getK8sModel() {
        return k8sModel;
    }

    @Override
    public void eventReceived(Watcher.Action action, Deployment deployment) {
        K8sNamespace namespace = k8sModel.getNamespaceByName(deployment.getMetadata().getNamespace());
        if (Action.ADDED.equals(action)) {
            if (namespace.getMode() == K8sNamespace.Mode.DYNAMIC) {
                addDeployment(namespace, deployment);
                eventConsumer.accept(namespace.getName());
            }
        } else if (Action.MODIFIED.equals(action)) {
            modifyDeployment(namespace, deployment);
            eventConsumer.accept(namespace.getName());
        } else if (Action.DELETED.equals(action)) {
            if (namespace.getMode() == K8sNamespace.Mode.DYNAMIC) {
                removeDeployment(namespace, deployment);
                eventConsumer.accept(namespace.getName());
            }
        } else {
            String name = deployment.getMetadata().getName();
            LOGGER.warn("Error watching deployment {}.", name);
        }
    }

    @Override
    public void onClose(KubernetesClientException cause) {
        // nothing to do here
    }

    public void register(Consumer<String> consumer) {
        this.eventConsumer = consumer;
    }
}
