package dev.ops.tools;

import dev.ops.tools.k8s.K8sController;
import dev.ops.tools.k8s.K8sDeployment;
import dev.ops.tools.k8s.K8sModel;
import dev.ops.tools.k8s.K8sNamespace;
import dev.ops.tools.midi.LaunchpadColor;
import dev.ops.tools.midi.LaunchpadDevice;
import dev.ops.tools.midi.MidiSystemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Launchpad controller implementation handles logic for button events and colors.
 */
public class K8sMinipadController extends LaunchpadDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sMinipadController.class);

    private final MidiSystemHandler midiSystem;
    private final K8sController k8sController;
    private String namespace;

    public K8sMinipadController(MidiSystemHandler midiSystem, K8sController k8sController, String namespace) {
        this.midiSystem = midiSystem;
        this.k8sController = k8sController;
        this.namespace = namespace;
    }

    public void initialize() {
        midiSystem.initialize(this);
        reset();
        update();

        k8sController.register(s -> {
            if (Objects.equals(namespace, s)) {
                update();
            }
        });
        k8sController.initialize();
    }

    @Override
    protected void handle(int command, int data1, int data2) {
        if (command == 176 && data2 == 127) {
            // a 1-8 button has been pressed
            LOGGER.info("Received MIDI event for 1-8 button [command={},data1={},data2={}]", command, data1, data2);

            int index = data1 - 104;
            this.namespace = k8sController.getK8sModel().getNamespace(index).getName();
            LOGGER.info("Selected namespace {}", this.namespace);

            update();

        } else if (command == 144 && data2 == 127) {
            boolean isAH = A_H_BUTTONS.contains(data1);
            if (isAH) {
                // a A-H button has been pressed
                LOGGER.info("Received MIDI event for A-H button [command={},data1={},data2={}]", command, data1, data2);

                int row = A_H_BUTTONS.indexOf(data1);
                K8sNamespace k8sNamespace = k8sController.getK8sModel().getNamespaceByName(namespace);
                if (row < k8sNamespace.getDeployments().size()) {
                    K8sDeployment k8sDeployment = k8sNamespace.getDeployment(row);
                    k8sController.scale(k8sNamespace, k8sDeployment, 0);
                }
            } else {
                // a square button has been pressed
                LOGGER.info("Received MIDI event for Square button [command={},data1={},data2={}]", command, data1, data2);

                int row = data1 / 16;
                int col = data1 % 16;

                K8sNamespace k8sNamespace = k8sController.getK8sModel().getNamespaceByName(namespace);
                K8sDeployment k8sDeployment = k8sNamespace.getDeployment(row);
                int replicas = k8sDeployment.getPods().size();
                if (col + 1 != replicas) {
                    k8sController.scale(k8sNamespace, k8sDeployment, col + 1);
                }
            }
        }
    }

    private void update() {
        updateNamespaceSelectors();
        updateGrid();
    }

    private void updateNamespaceSelectors() {
        K8sModel k8sModel = k8sController.getK8sModel();
        for (int i = 0; i < k8sModel.getNamespaces().size(); i++) {
            K8sNamespace ns = k8sModel.getNamespace(i);
            if (Objects.equals(ns.getName(), namespace)) {
                top(i, LaunchpadColor.BRIGHT_AMBER);
            } else {
                top(i, LaunchpadColor.DARK_AMBER);
            }
        }
    }

    private void updateGrid() {
        K8sNamespace ns = k8sController.getK8sModel().getNamespaceByName(namespace);
        int size = ns.getDeployments().size();
        for (int i = 0; i < size && i < 8; i++) {
            K8sDeployment deployment = ns.getDeployment(i);
            LOGGER.info("Displaying Deployment {} at row {}", deployment.getName(), i);

            Collection<String> status = deployment.getPods().values();
            List<LaunchpadColor> colors = status.stream().map(LaunchpadColor::forStatus).collect(Collectors.toList());

            clearRow(i);
            colorRow(i, colors);
            right(i, LaunchpadColor.BRIGHT_RED);
        }

        // clear any unused rows
        for (int i = size; i < 8; i++) {
            clearRow(i);
        }
    }
}
