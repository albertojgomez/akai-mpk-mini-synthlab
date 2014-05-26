StormServer  {
	var <>scServer,<>midi,<>gui, <>instruments;

	*new {
		^super.new.init;
    }

	init{
		instruments = List.new;
		{
			/****  init sc server   *****/
			scServer = Server.default;
			scServer.options.memSize = 2.pow(20);
			//check ServerOptions.outDevices to change this
			scServer.options.outDevice = "Lexicon Alpha In/Out";
			scServer.options.inDevice = "Lexicon Alpha In/Out";
			scServer.options.sampleRate=48000;
			scServer.boot;
			scServer.doWhenBooted({
				/****  init midi   *****/
				midi = StormMidi();
				/****  init GUI   *****/
				gui = StormGUI();
				this.initAudioIn();
			});
		}.fork;
		^this;
	}
	initAudioIn{
		SynthDef("help-AudioIn2",{ arg out=[0,1];
		~rightIn = AudioIn.ar(1);
		Out.ar(Compander.ar( out),
				[~rightIn,~rightIn]*0.5
			)
		}).play;

		SynthDef("help-AudioIn",{ arg out=[0,1];
			~leftIn= AudioIn.ar(2);
			Out.ar(out,
			Splay.ar(GVerb.ar(~leftIn,70,6,mul:0.01)) + (~leftIn * 0.6)
			)
		}).play;
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
			~serverSingleton.scServer.sync
		}.fork;
		^~serverSingleton;
	}

	*sync{
		{
		StormServer.s.scServer.sync;
		}.fork;
	}

	*getStormGUI{
		^StormServer.s.gui;
	}

	*getStormMidi{
		^StormServer.s.midi;
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
