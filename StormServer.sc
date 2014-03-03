StormServer  {
	var <>scServer,<>midi,<>gui, <>instruments,<>presetsFolder,<>sessionFolder;

	*new {
		^super.new.init;
    }

	init{
		instruments = List.new;
		presetsFolder = Platform.userExtensionDir ++ '/';
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

	*initSession {
		|folder = nil|
		if (folder.isNil,{
			StormServer.s.sessionFolder = Platform.userAppSupportDir;
		},{
			StormServer.s.sessionFolder = folder;
		});
		Archive.archiveDir = folder;
		if (File.exists(folder ++ '/archive.sctxar'),{
			var session;
			Archive.read;
			session = Archive.global.at(\StormSession);
			session.do({
				|stormsynth|
				Server.default.doWhenBooted({
					StormSynth(stormsynth[\name],stormsynth[\graphFunc]);
				});
			});
		});
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

	//Getting bus value is an async action, so we have to do it in a funny way
	*saveSession{
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
				stormsynth.buses[paramKey].get({
					|busVal|
					synthArchive[\params][paramKey] = busVal;
					//finished creating archive object
					if (synthArchive[\params].size == stormsynth.controlEvent.keys.size){
						allSynths.add(synthArchive);
						if (allSynths.size == StormServer.s.instruments.size){
							Archive.global.put(\StormSession,allSynths);
							Archive.write;
						}
					}
				});

			});
		})
	}

}
