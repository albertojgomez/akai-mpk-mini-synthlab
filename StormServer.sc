StormServer {
	classvar <>scServer, <>view, <>clock;

	*initClass{
		StartUp.add({
			/****  init sc server   *****/
			scServer = Server.default;
			StormServer.initAudio();
			scServer.boot;
			scServer.doWhenBooted({
				/****  init GUI   *****/
				view = Window.new("⚡⚡⚡⚡._-5t0rmb0tn3t-_.⚡⚡⚡⚡", 1280@800).front;
				/****  init midi   *****/
				StormMidi.initialize();
				/****  init clock   *****/
				TempoClock.default.tempo = 135*4/60;
				clock = TempoClock.default;
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

	*panic{
		Server.freeAll;
	}

}
