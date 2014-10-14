StormMidi  {
	classvar <>midiOut, <>connectTo, <>midiInterface, <>midiIn,<>midiInstruments ;
	*initialize{
		//TODO change everything to static (only one stormmidi needed)
		//heavy use of class var
		var inDevice;
		MIDIClient.init;
		MIDIIn.connectAll;
		midiOut = MIDIOut.newByName("IAC Driver", "Bus 1");
		inDevice = MIDIIn.findPort("MPK mini", "MPK mini");
		if (inDevice.isNil,{
			midiIn = 0;
		},{
				midiIn = MIDIIn.findPort("MPK mini", "MPK mini").uid;
				//bind noteonand noteoff  to synths in the array depending on channel
				MIDIFunc.noteOn ({
					|velo, note, channel|
					velo.postln;
					note.postln;
					channel.postln;
					if (StormSynth.synths.atIndex(channel).isNil.not){
						StormSynth.synths.atIndex(channel).noteOn(note);
					}
				}, srcID: midiIn);
				MIDIFunc.noteOff ({
					|velo, note, channel|
					if (StormSynth.synths.atIndex(channel).isNil.not){
						StormSynth.synths.atIndex(channel).noteOff(note);
					}
				}, srcID: midiIn);
		});

		connectTo = MIDIIn.findPort("IAC Driver", "Bus 1").uid;
		midiOut.latency = 0;

		^this
	}

	*new {
		^super.new.init;
    }
	init{
		var inDevice;
		MIDIClient.init;
		MIDIIn.connectAll;
		midiOut = MIDIOut.newByName("IAC Driver", "Bus 1");
		inDevice = MIDIIn.findPort("MPK mini", "MPK mini");
		if (inDevice.isNil,{
			midiIn = 0;
			"ASDASDASDASDASDASDASDAS".postln;
		},{
				"TYRTRYTRYRTYTRRY".postln;
			midiIn = MIDIIn.findPort("MPK mini", "MPK mini").uid;
				MIDIFunc.noteOn ({
					|note,a,b,c|
					note.postln;
					if (StormSynth.synths.[StormSynth.synths.keyAt()].isNil.not){
						StormSynth.synths.[StormSynth.synths.keyAt()].noteOn(note);
					}
				}, srcID: midiIn)
		});

		connectTo = MIDIIn.findPort("IAC Driver", "Bus 1").uid;
		midiOut.latency = 0;

		^this
	}




}

