StormServer  {
	var <>variablev,<>midiCounter,<s,<midiDevice;

	*singleton{
		if(~single.isNil,{~single = StormServer()});
		{~single.s.sync}.fork;
		^~single;
	}

	*new {
		^super.new.init;
    }

	init{
		MIDIIn.connectAll;
		midiDevice = MIDIIn.findPort("MPK mini", "MPK mini").uid;
		{
			s = Server.default;
			s.options.memSize = 2.pow(20);
			s.options.outDevice="Built-in Output";
			//s.options.sampleRate=48000;
			//s.options.numOutputBusChannels = 64;
			s.bootSync;

			s.sync;
		}.fork;
		midiCounter = 0;
		this.initGUI();

		^this;

	}

	*sync{
		{
		StormServer.singleton.s.sync;
		}.fork;
	}

	*getDevice{
		^StormServer.singleton.midiDevice;
	}

	initGUI{

	}

	initMidi{

	}

	*getMidiChannel{
		var temp;
		temp = StormServer.singleton.midiCounter;
		StormServer.singleton.midiCounter = StormServer.singleton.midiCounter + 1;
		^temp;
	}

	method{
		^Nil;
	}

	*staticMethod{
		^Nil;
	}


}
