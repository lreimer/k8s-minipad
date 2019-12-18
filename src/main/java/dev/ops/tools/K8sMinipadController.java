package dev.ops.tools;

import dev.ops.tools.midi.LaunchpadDevice;
import dev.ops.tools.midi.MidiSystemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launchpad controller implementation handles logic for button events and colors.
 */
public class K8sMinipadController extends LaunchpadDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sMinipadController.class);

    private final MidiSystemHandler midiSystem;

    public K8sMinipadController(MidiSystemHandler midiSystem) {
        this.midiSystem = midiSystem;
    }

    public void initialize() {
        midiSystem.initialize(this);
        reset();
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

    @Override
    public void close() {
        super.close();
    }
}
