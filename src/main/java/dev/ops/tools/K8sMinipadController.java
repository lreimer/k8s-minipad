package dev.ops.tools;

import dev.ops.tools.k8s.K8sConfig;
import dev.ops.tools.midi.LaunchpadColor;
import dev.ops.tools.midi.LaunchpadDevice;
import dev.ops.tools.midi.MidiSystemHandler;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Launchpad controller implementation handles logic for button events and colors.
 */
public class K8sMinipadController extends LaunchpadDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sMinipadController.class);

    private final MidiSystemHandler midiSystem;
    private final KubernetesClient client;
    private final K8sConfig config;

    private String namespace;

    public K8sMinipadController(MidiSystemHandler midiSystem, KubernetesClient client, File configFile, String namespace) {
        this.midiSystem = midiSystem;
        this.client = client;
        this.config = K8sConfig.fromFile(configFile);
        this.namespace = namespace;
    }

    public void initialize() {
        LOGGER.info("Using K8s config {}", config);
        midiSystem.initialize(this);
        reset();

        for (int i = 0; i < config.getNamespaces().size(); i++) {
            top(i, LaunchpadColor.BRIGHT_AMBER);
        }
    }

    @Override
    protected void handle(int command, int data1, int data2) {
        if (command == 176 && data2 == 127) {
            // a 1-8 button has been pressed
            LOGGER.info("Received MIDI event for 1-8 button [command={},data1={},data2={}]", command, data1, data2);

        } else if (command == 144 && data2 == 127) {
            // a A-H button has been pressed
            LOGGER.info("Received MIDI event for A-H button [command={},data1={},data2={}]", command, data1, data2);

            int row = A_H_BUTTONS.indexOf(data1);
        }
    }
}
