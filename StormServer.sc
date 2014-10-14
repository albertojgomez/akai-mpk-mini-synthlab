StormServer {
	classvar <>scServer, <>view, <>clock;

	*initClass{
		StartUp.add({
			/****  init sc server   *****/
			scServer = Server.default;
			StormServer.initAudio();
			scServer.boot;
			scServer.doWhenBooted({
				/****  init clock   *****/
				TempoClock.default.tempo = 125*4/60;
				clock = TempoClock.default;
				/****  init GUI   *****/
				view = Window.new("⚡⚡⚡⚡._-5t0rmb0tn3t-_.⚡⚡⚡⚡", 1280@800).front;
				view.view.decorator = FlowLayout( view.bounds, 0@0, 0@0 );
				/****  init midi   *****/
				StormMidi.initialize();
				/* Init Synth resources*/
				StormSynth.initialize();
			});
		});
	}

	*initAudio{
		//no long methods
		StormServer.setAudioOptions();
		StormServer.initAudioIn();
		StormServer.initAudioOut();
	}

	*setAudioOptions{
		scServer.options.memSize = 2.pow(20);
		//check ServerOptions.outDevices to change this
		scServer.options.sampleRate=48000;
		if (ServerOptions.outDevices.includesEqual("Lexicon Alpha In/Out"),{
			scServer.options.outDevice = "Lexicon Alpha In/Out";
		}
		,{
			scServer.options.outDevice ="Built-in Output";
		});
		if (ServerOptions.inDevices.includesEqual("Lexicon Alpha In/Out"),{
			scServer.options.inDevice = "Lexicon Alpha In/Out";
		}
		,{
			scServer.options.inDevice = nil;
		});

	}

	*initAudioIn{
		//Needs to go in StormDrums which NEEDS to inherit from Storm synths
		if (scServer.options.inDevice == "Lexicon Alpha In/Out",{
			scServer.doWhenBooted({
				SynthDef("AudioIn",{
					Out.ar(0,
						Splay.ar([AudioIn.ar(1), FreeVerb.ar(
							AudioIn.ar(2))],0.1 ) * 2);

				}).play;
				/*SynthDef("help-AudioIn2",{
					Out.ar(0, AudioIn.ar(1) ! 2)
				}).play;
				SynthDef("help-AudioIn",{
					Out.ar(0, AudioIn.ar(2) ! 2);
				}).play;*/
			});
		});
	}

	*initAudioOut{
		{
			scServer.doWhenBooted({
				SynthDef("ReplaceOut", {
					var masterOutRight = In.ar(1),
					masterOutLeft = In.ar(0);
					//do what you need with master
					ReplaceOut.ar(1,  masterOutRight);
					ReplaceOut.ar(0,  masterOutLeft);
				}).send(scServer);
				scServer.sync;
				Synth.tail(scServer, "ReplaceOut");
			});

		}.fork;
	}

	*schedule{
		|function|
		clock.schedAbs(clock.nextTimeOnGrid(64), function);
	}

	*panic{
		Server.freeAll;
	}

}
