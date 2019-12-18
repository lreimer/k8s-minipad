package dev.ops.tools.midi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;

/**
 * Handler implementation to obtain Receiver and Transmitter from MIDI system.
 */
public class MidiSystemHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiSystemHandler.class);

    private static final String DEVICE_NAME = "Launchpad Mini";

    static {
        // this seems to be required at least under Windows
        System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#" + DEVICE_NAME);
        System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#" + DEVICE_NAME);
    }

    private Transmitter transmitter;
    private Receiver receiver;

    public void infos() {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            LOGGER.info("Found attached MIDI device {} ({}) - {}", info.getName(), info.getVersion(), info.getDescription());
        }
    }

    public void initialize(LaunchpadDevice device) {
        LOGGER.info("Initializing MIDI system.");

        try {
            this.transmitter = MidiSystem.getTransmitter();
            this.transmitter.setReceiver(device);
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException("Unable to get Transmitter from MIDI system.", e);
        }

        try {
            this.receiver = MidiSystem.getReceiver();
            device.setReceiver(receiver);
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException("Unable to get Receiver from MIDI system.", e);
        }
    }

    public void destroy() {
        LOGGER.info("Shutdown MIDI system.");

        this.transmitter.close();
        this.receiver.close();
    }
}
