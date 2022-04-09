package entity;

import javax.sound.midi.*;

public class MidiTest{

    Instrument[] instr;
    MidiChannel[] mChannels;

    public MidiTest() {
        try{
            Synthesizer midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();

            //get and load default instrument and channel lists
            this.instr = midiSynth.getDefaultSoundbank().getInstruments();
            this.mChannels = midiSynth.getChannels();

            midiSynth.loadInstrument(instr[1]);//load an instrument
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playNote(double rpm){
        // min - max = {30, 65}
//        int noteNum = 30 + (int)(rpm * 0.00269);
//        int pitchBend = 8196 + (int)( (8196) * ((rpm * 0.00269) % 1) );
        int noteNum = 40 + (int)(rpm * 0.00307);
        int pitchBend = 8196 + (int)( (8196) * ((rpm * 0.00307) % 1) );

        mChannels[0].setPitchBend(pitchBend);
        mChannels[0].noteOn(noteNum, 30); //On channel 0
        // octave up
        mChannels[1].setPitchBend(pitchBend);
        mChannels[1].noteOn(noteNum + 12, 20); //On channel 0
        // 2 octave up
        mChannels[2].setPitchBend(pitchBend);
        mChannels[2].noteOn(noteNum + 24, 15); //On channel 0
        // octave down
        mChannels[3].setPitchBend(pitchBend);
        mChannels[3].noteOn(noteNum - 12, 20); //On channel 0

        try { Thread.sleep(1); // wait time in milliseconds to control duration
        } catch( InterruptedException e ) {
            e.printStackTrace();
        }
        mChannels[0].noteOff(noteNum); //turn of the note
        mChannels[1].noteOff(noteNum); //turn of the note
        mChannels[2].noteOff(noteNum); //turn of the note
        mChannels[3].noteOff(noteNum); //turn of the note

//        mChannels[0].getPitchBend();
    }

}   