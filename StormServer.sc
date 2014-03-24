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
			scServer.options.sampleRate=48000;
			scServer.boot;
			scServer.doWhenBooted({
				/****  init midi   *****/
				midi = StormMidi();
				/****  init GUI   *****/
				gui = StormGUI();
			});
		}.fork;
		^this;
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

}
