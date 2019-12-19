package dev.ops.tools.midi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract Launchpad MIDI device implementation to encapsulate the usage of Javax Sound Midi.
 */
public abstract class LaunchpadDevice implements Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchpadDevice.class);

    private Receiver receiver;

    protected static final List<Integer> A_H_BUTTONS = Arrays.asList(8, 24, 40, 56, 72, 88, 104, 120);

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage shortMessage = (ShortMessage) message;
            handle(shortMessage.getCommand(), shortMessage.getData1(), shortMessage.getData2());
        }
    }

    protected abstract void handle(int command, int data1, int data2);

    @Override
    public void close() {
        LOGGER.info("Closing Launchpad device.");
        reset();
        receiver.close();
    }

    public void top(int col, LaunchpadColor color) {
        int data1 = 104 + col;
        try {
            receiver.send(new ShortMessage(176, 0, data1, color.getValue()), -1);
        } catch (InvalidMidiDataException e) {
            LOGGER.warn("Could not send ShortMessage.", e);
        }
    }

    public void right(int row, LaunchpadColor color) {
        int data1 = A_H_BUTTONS.get(row);
        try {
            receiver.send(new ShortMessage(144, 0, data1, color.getValue()), -1);
        } catch (InvalidMidiDataException e) {
            LOGGER.warn("Could not send ShortMessage.", e);
        }
    }

    public void square(int row, int col, LaunchpadColor color) {
        int data1 = (row * 16) + col;
        try {
            receiver.send(new ShortMessage(144, 0, data1, color.getValue()), -1);
        } catch (InvalidMidiDataException e) {
            LOGGER.warn("Could not send ShortMessage.", e);
        }
    }

    protected void clearRow(int row) {
        for (int j = 0; j < 9; j++) {
            square(row, j, LaunchpadColor.NONE);
        }
    }

    protected void colorRow(int row, List<LaunchpadColor> colors) {
        for (int j = 0; j < colors.size(); j++) {
            square(row, j, colors.get(j));
        }
    }

    /**
     * Rest all square buttons to no color.
     */
    public void reset() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 9; j++) {
                square(i, j, LaunchpadColor.NONE);
            }
            top(i, LaunchpadColor.NONE);
        }
    }
}
