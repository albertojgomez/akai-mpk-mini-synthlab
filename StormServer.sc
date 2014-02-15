StormServer  {
	var <>variablev,<>midiCounter,<s,<midiDevice, <>window, <>sequencerTracks,<>clock;

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
		midiDevice = MIDIIn.findPort("IAC Driver", "Bus 1").uid;
		//midiDevice = MIDIIn.findPort("MPK mini", "MPK mini").uid;
		{
			s = Server.default;
			s.options.memSize = 2.pow(20);
			s.options.outDevice="Built-in Output";
			//s.options.sampleRate=48000;
			//s.options.numOutputBusChannels = 64;
			s.bootSync;
			s.sync;
			TempoClock.default.tempo = 135*4/60;
			clock = TempoClock.default;
			sequencerTracks = Array.new(4);
			4.do({
				|i|
				sequencerTracks.add(PbindProxy.new.set(\freq,\rest));
			});
			~seqCounter = 0;
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

	initGUI{
		window = Window.new("St0rmB0tn3t", 1000@500).front;
		window.view.decorator = FlowLayout( window.view.bounds, 10@10, 5@5 );
	}

	*getGUI{
		|synthlab|

		^View(StormServer.singleton.window,1000@100);
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
				notematrix[ note ].get(\gate).postln;
				notematrix[ note ].release
			});

			notematrix[ note ] = Synth(name,params);

		},chan:midiChannel,srcID:StormServer.getDevice);
		MIDIFunc.noteOff({
			arg ...args;
			var note;
			note = args[1] ;
			'b4'.post;
			notematrix[ note ].release;
			'afert'.postln;

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
				note.postln;
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




}
