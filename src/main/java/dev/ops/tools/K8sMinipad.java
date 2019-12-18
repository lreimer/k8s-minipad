package dev.ops.tools;

import dev.ops.tools.midi.MidiSystemHandler;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

/**
 * Main application for the K8s Minipad.
 */
@Command(version = "K8s Minipad 1.0", mixinStandardHelpOptions = true)
class K8sMinipad implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sMinipad.class);

    @Option(names = {"-n", "--namespace"}, defaultValue = "default", description = "the K8s namespace")
    private String namespace;

    @Option(names = {"-f", "--file"}, paramLabel = "JSON_CONFIG", description = "the configuration file", required = true)
    private File configFile;

    public static void main(String[] args) {
        CommandLine.run(new K8sMinipad(), args);
    }

    @Override
    public void run() {
        LOGGER.info("Running K8s Minipad ...");

        MidiSystemHandler midiSystem = new MidiSystemHandler();
        midiSystem.infos();

        KubernetesClient client = new DefaultKubernetesClient();
        K8sMinipadController controller = new K8sMinipadController(midiSystem, client, configFile, namespace);
        controller.initialize();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown K8s Minipad.");
            controller.close();
            client.close();
            midiSystem.destroy();
        }));
    }
}
