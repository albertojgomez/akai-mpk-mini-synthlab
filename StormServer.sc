StormServer  {
	var <>variablev,<>midiCounter,<s,<midiDevice,
	<>window, <>sequencerTracks,<>clock,<>size,<>midi;

	*singleton{
		if(~single.isNil,{~single = StormServer()});
		{~single.s.sync}.fork;
		^~single;
	}

	*new {
		^super.new.init;
    }

	init{
		size = 0.7;
		~seqCounter = 0;
		{
			/****  init sc server   *****/
			s = Server.default;
			s.options.memSize = 2.pow(20);
			//s.options.outDevice="Soundflower (64ch)";
			//s.options.sampleRate=44100;
			//s.options.numOutputBusChannels = 64;
			//s.options.outDevice="Built-in Output";
			s.options.outDevice = "Lexicon Alpha In/Out";
			s.options.sampleRate=48000;
			s.bootSync;
			s.sync;
			/****  init midi   *****/
			midi = StormMidi();
			midiDevice = midi.connectTo;
			/****  init sequencers   *****/
			TempoClock.default.tempo = 135*4/60;
			clock = TempoClock.default;
			sequencerTracks = Array.new(4);
			4.do({
				|i|
				sequencerTracks.add(PbindProxy.new.set(\freq,\rest));
			});
			clock.schedAbs(clock.nextTimeOnGrid(64), { Ppar(sequencerTracks).play(quant:4) });
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

	*getClock{
		^StormServer.singleton.clock;
	}

	initGUI{
		window = Window.new("St0rmB0tn3t", 1000@500).front;
		window.view.decorator = FlowLayout( window.view.bounds, 0@0, 0@0 );
	}

	*getGUI{
		|synthlab|

		^View(StormServer.singleton.window,(1000@240)*StormServer.guiSize);
	}

	initMidi{

	}

	*getMidiChannel{
		var temp;
		temp = StormServer.singleton.midiCounter;
		StormServer.singleton.midiCounter = StormServer.singleton.midiCounter + 1;
		^temp;
	}

	*getSequencerTrack{
		var temp;
		temp = ~seqCounter;
		~seqCounter  = ~seqCounter+1;
		^StormServer.singleton.sequencerTracks[temp];
	}

	*connectMidi{
		|synthlab|
		var name = synthlab.name,midiChannel = synthlab.midiChannel,
			notematrix = synthlab.notematrix,
		activePanel = synthlab.activePanel,
		midiCCSelectChannel = synthlab.midiCCSelectChannel,

		panels = synthlab.panels,
		knobs = synthlab.knobs;
		StormServer.getMidi.patch(synthlab);

		//Notes
		MIDIFunc.noteOn({
			arg ...args;
			var note,params;
			note = args[1] ;
			params = synthlab.getParamsArray();
			params.add(\freq);
			params.add(note.midicps);
			if (notematrix[ note ].notNil,{
				notematrix[ note ].release
			});

			notematrix[ note ] = Synth(name,params);

		},chan:midiChannel,srcID:StormServer.getDevice);
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
				knobIndex = (synthlab.activePanel * 8) + ccNumber - 1;
				if (knobs.at(knobIndex).notNil,{
					knobs.at(knobIndex).valueAction_( value / 127);
				});
			}.defer;
		},chan:midiChannel,srcID:StormServer.getDevice);
		MIDIFunc.noteOn({
			|velocity,note|
			if(panels.at(note).notNil,{
				synthlab.setActivePanel(note);
			});
		},chan:midiCCSelectChannel,srcID:StormServer.getDevice);
	}

	method{
		^Nil;
	}

	*staticMethod{
		^Nil;
	}

	*guiSize{
		^StormServer.singleton.size;
	}

	*getMidi{
		^StormServer.singleton.midi;
	}




}
