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

	*connectMidi{
		|synthlab|
		var name = synthlab.name,midiChannel = synthlab.midiChannel,notematrix = synthlab.notematrix,
		activePanel = synthlab.activePanel,
		midiCCSelectChannel = synthlab.midiCCSelectChannel,

		panels = synthlab.panels,
		knobs = synthlab.knobs;
		//midiChannel = synthlab.activePanel,

		//Notes
			MIDIFunc.noteOn({
				arg ...args;
				var note,params;
				note = args[1] ;
				name.postln;
				params = synthlab.getParamsArray();
				params.add(\freq);
				params.add(note.midicps);
				if (notematrix[ note ].notNil,{
					notematrix[ note ].release
				});

				notematrix[ note ] = Synth(name,params);

			},chan:midiChannel,srcID:StormServer.getDevice);
			("will listen on "++ midiChannel).postln;
			MIDIFunc.noteOff({
				arg ...args;
				var note;
				note = args[1] ;
				notematrix[ note ].release;

			},chan:midiChannel,srcID:StormServer.getDevice);
			//Knobs
			MIDIFunc.cc({
				|value,ccNumber|
				{
					var knobIndex;
					knobIndex = (activePanel * 8) + ccNumber - 1;
					if (knobs.at(knobIndex).notNil,{
						knobs.at(knobIndex).valueAction_( value / 127);
					});
				}.defer;
			},chan:midiChannel,srcID:StormServer.getDevice);
			MIDIFunc.noteOn({
				|velocity,note|
				if(panels.at(note).notNil,{
					this.setActivePanel(note);
				});
			},chan:midiCCSelectChannel,srcID:StormServer.getDevice);
	}

	method{
		^Nil;
	}

	*staticMethod{
		^Nil;
	}




}
