package ru.waxera.chat.io.client.sound;

import javax.sound.sampled.AudioFormat;

public final class ApplicationAudioFormat extends AudioFormat {
    private static AudioFormat instance;

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private ApplicationAudioFormat() {
        super(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        instance = this;
    }

    public static AudioFormat getInstance(){
        return instance == null ? instance = new ApplicationAudioFormat() : instance;
    }
}
