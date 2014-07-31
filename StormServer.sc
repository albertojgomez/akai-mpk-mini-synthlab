StormServer  {
	var <>scServer,<>midi,<>gui, <>instruments,<>clock, <>sequencerTracks;

	*initClass{
		StartUp.add({
			StormServer.s;
		});
	}

	*new {
		^super.new.init;
    }

	init{
		instruments = List.new;
		{
			/****  init sc server   *****/
			scServer = Server.default;
			this.setServerOptions();
			scServer.boot;
			scServer.doWhenBooted({
				/****  init midi   *****/
				midi = StormMidi();
				/****  init GUI   *****/
				gui = StormGUI();
				/****  init audio in   *****/
				this.initAudioIn();
				/****  init sequencers   *****/
				TempoClock.default.tempo = 135*4/60;
				clock = TempoClock.default;
				//16 drum tracks + 3 synths
				sequencerTracks = Array.new(19);
				19.do({
					|i|
					sequencerTracks.add(PbindProxy.new.set(\freq,\rest));
				});
				clock.schedAbs(clock.nextTimeOnGrid(64), { Ppar(sequencerTracks).play(quant:4) });
			});
		}.fork;
		^this;
	}

	setServerOptions{
		scServer.options.memSize = 2.pow(20);
		scServer.options.sampleRate=48000;
		//check ServerOptions.outDevices to change this
		if (ServerOptions.outDevices.includesEqual("Lexicon Alpha In/Out"),{
			scServer.options.outDevice = "Lexicon Alpha In/Out";
		}
		,{
			scServer.options.outDevice = "Built-in Output";
		});
		if (ServerOptions.inDevices.includesEqual("Lexicon Alpha In/Out"),{
			scServer.options.inDevice = "Lexicon Alpha In/Out";
		}
		,{
			scServer.options.inDevice = nil;
		});

	}

	initAudioIn{
		if (scServer.options.outDevice == "Lexicon Alpha In/Out",{
			SynthDef("help-AudioIn2",{ arg out=[0,1];
				~rightIn = AudioIn.ar(1);
				Out.ar(Compander.ar( out),
					[~rightIn,~rightIn]*0.5
				)
			}).play;

			SynthDef("help-AudioIn",{
				arg out=[0,1];
				var in  = AudioIn.ar(2);
				in = Compander.ar(in,Amplitude.ar(in),0.001
					slopeBelow: 100,
					slopeAbove: 10,
					clampTime: 0.1,
					relaxTime: 0.1
				);
				~leftIn= in + DelayN.ar(LocalIn.ar(2), 0.3, 0.3);
				Out.ar(out,
					Splay.ar(GVerb.ar(~leftIn,100,6,mul:0.01)) + (~leftIn * 0.6)
				);
				LocalOut.ar(~leftIn.reverse * 0.1);
			}).play;
		});
	}


	*loadSession {
		|sessionFile = nil|
		var session;
		StormServer.s;
		if(sessionFile.isNil || File.type(sessionFile) != \regular,
		{
			"Error, no session file provided".postln;
			^nil;
		});

		Archive.read(sessionFile);
		session = Archive.global.at(\StormSession);
		session.do({
			|stormsynth|
			Server.default.doWhenBooted({
				StormSynth(stormsynth[\name],stormsynth[\graphFunc]);
				StormServer.s.scServer.sync;
				stormsynth[\params].keys.do({
					|paramKey|
					{
						StormServer.s.gui.instrumentGUIs[stormsynth[\name]][\knobs][paramKey]
						.valueAction_(stormsynth[\params][paramKey])
					}.defer;
				});
			});
		});
	}

	//Getting bus value is an async action, so we have to do it in a funny way
	*saveSession{
		|sessionFile|
		var allSynths = List.new;
		StormServer.s.instruments.do({
			|stormsynth|
			var synthArchive;
			synthArchive = (
				\name : stormsynth.name,
				\graphFunc : stormsynth.graphFunction,
				\params : ()
			);
			stormsynth.controlEvent.keys.do({
				|paramKey|
				synthArchive[\params][paramKey] =
				StormServer.s.gui.instrumentGUIs[stormsynth.name][\knobs][paramKey].value;
			});
			allSynths.add(synthArchive);
		});
		Archive.global.put(\StormSession,allSynths);
		Archive.write(sessionFile);
	}

	/* Singleton pattern*/
	*s{
		if(~serverSingleton.isNil,{~serverSingleton = StormServer()});
		{
			~seqCounter = 0;
			~serverSingleton.scServer.sync
		}.fork;
		^~serverSingleton;
	}

	*sync{
		{
		StormServer.s.scServer.sync;
		}.fork;
	}

	*getClock{
		^StormServer.s.clock;
	}

	*getStormGUI{
		^StormServer.s.gui;
	}

	*getStormMidi{
		^StormServer.s.midi;
	}

	*getSequencerTrack{
		var temp;
		temp = ~seqCounter;
		~seqCounter  = ~seqCounter+1;
		^StormServer.s.sequencerTracks[temp];
	}


	*addInstrument{
		|stormsynth|
		^StormServer.s.instruments.add(stormsynth);

	}

	*instrumentExists{
		|name|
		StormServer.s.instruments.do({
			|i|
			if (i.name == name){
				^i;
			};
		});
		^false;
	}

	*panic{
		StormServer.s.midi.midiInstruments.do({
			|instrument|
			instrument[\notematrix].do({|node| node.set(\gate,0);node.release; "release".postln;})
		});
		Server.freeAll;
	}

}
