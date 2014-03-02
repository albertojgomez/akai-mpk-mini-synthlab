StormServer  {
	var <>scServer,<>midi,<>gui, <>instruments;

	*new {
		^super.new.init;
    }

	init{
		instruments = ();
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

}
