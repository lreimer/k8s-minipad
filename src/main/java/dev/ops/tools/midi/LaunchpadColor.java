package dev.ops.tools.midi;

/**
 * Enum for the different supported colors.
 */
public enum LaunchpadColor {
    NONE(64),

    DARK_RED(13),
    MEDIUM_RED(14),
    BRIGHT_RED(15),

    DARK_YELLOW(61),
    MEDIUM_YELLOW(62),
    BRIGHT_YELLOW(63),

    DARK_AMBER(29),
    MEDIUM_AMBER(30),
    BRIGHT_AMBER(31),

    DARK_GREEN(16),
    MEDIUM_GREEN(28),
    BRIGHT_GREEN(60);

    private final int value;

    LaunchpadColor(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
