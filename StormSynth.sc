StormSynth : StormObject  {
	classvar width = 60 , height = 60, <>synths;
	var <>controlEvent, <>buses, <>name, <>paramNames, <>graphFunction, <>sequencer;

	*initClass{
		synths = ();
	}

	*new {
		|graphFunc,synthName|
		if (synthName.isNil){
			synthName = "StormSynth_" ++ synths.size;
		};
		^super.new.initStormSynth(synthName,graphFunc);
    }

	initStormSynth{
		|synthName, graphFunc|
		var prevKnobs = false;
		name = synthName;
		graphFunction = graphFunc;
		controlEvent = ();
		buses = () ;
		paramNames = List.new();
		if(StormServer.instrumentExists(synthName) != false){
			//get controlevent
			prevKnobs = ();
			StormServer.getStormGUI.instrumentGUIs[synthName][\knobs].keys.do({
				|i|
				prevKnobs[i] = StormServer.getStormGUI.instrumentGUIs[synthName][\knobs][i].value;
			});
			StormServer.instrumentExists(synthName).destroy;
		};
		//wrap in routine so that we can sync with server and get controls
		{
			//do synth def
			SynthDef( name , {
				| gate = 1, env_attack = 0.5, env_decay  = 0.5, env_sustain = 1, env_release = 1,
				filter_attack = 0.5, filter_decay  = 0.5, filter_sustain = 1, filter_release  = 1,
				filter_cutoff=10000, filter_reso=3 ,vol = 1|
				var signal,env,amp,fenv,filter;
				//amp env
				env=Env.adsr(
					attackTime:env_attack,
					decayTime:env_decay,
					sustainLevel:env_sustain,
					releaseTime:env_release );
				amp=EnvGen.kr(env, gate);
				//filter env
				fenv=Env.adsr(
					attackTime:filter_attack,
					decayTime:filter_decay,
					sustainLevel:filter_sustain,
					releaseTime:filter_release );
				filter=EnvGen.kr(fenv,gate:gate).exprange(50,filter_cutoff);
				signal = SynthDef.wrap(graphFunc);
				signal = MoogFF.ar( signal,filter,filter_reso,0) ;
				signal = signal * amp * vol;
				Out.ar( [0,1]  , signal );
				DetectSilence.ar(signal,doneAction:2);
			} ).add;

			SynthDescLib.global.synthDescs.at(name).controls.do({
				|control|
				var exclude = [\freq , \gate];
				if ( [\freq , \gate].includesEqual(control.name) ){
					paramNames.add(control.name);
					controlEvent[ control.name ] = this.makecSpec(control);
					buses [ control.name ] = Bus.control.set(
						controlEvent[ control.name ].default
					);
				};

			});
			StormServer.sync;
			StormServer.getStormGUI.addSynth(this);
			StormServer.getStormMidi.connectSynth(this);
			StormServer.addInstrument(this);
			//restore previous values if there are
			if (prevKnobs != false){
				StormServer.getStormGUI.instrumentGUIs[synthName][\knobs].keys.do({|i|
					{
						if (prevKnobs[i].isNil.not
						&&
						StormServer.getStormGUI.instrumentGUIs[synthName][\knobs][i].isNil.not){
							StormServer.getStormGUI.instrumentGUIs[synthName][\knobs][i].valueAction_(prevKnobs[i]);
						}
					}.defer
				});
			};
			sequencer = StormPattern(this);
		}.fork;

		^this;
	}
	/*Return argument pairs list to use with Synth()*/
	getParamsArray{
		var e = List[] ;
		controlEvent.do({
			|spec|
			e.add( spec.units );
			e.add( buses[ spec.units ].asMap );
		});
		^e;
	}
	/* Return ControlSpec to handle a given Synth parameter */
	/* paramameters are usually group_name */
	makecSpec{
		|control|
		var cspec, group, specData, nameParts, name;
		nameParts = control.name.asString.split($_);
		if (nameParts.size == 2 ,{
			group = nameParts[0];
			name = nameParts[1];
		},{
			name = control.name.asString;
			group = 'randomString';
		});
		//Get control type (log, lin, range)
		specData = StormSynth.paramTypes( name.asSymbol );
		cspec = ControlSpec(
			specData[1] ,  specData[2]  ,
			specData[0] , 0 ,  control.defaultValue  ,control.name
		);
		^cspec;
	}

	/* Returns warp, min and max of a parameter with a given name.Defaults to \lin,0,1 */
	*getParamType{
		|paramName|
		var known = (
			\freq : [\exp, 20, 20500],
			\cutoff : [\exp, 20, 20500],
			\attack : [\lin, 0, 10] ,
			\decay : [\lin, 0, 10] ,
			\release : [\lin, 0.0001, 10]
		);
		if (known[paramName].isNil){
			//zero in  some param make the whole thing go apeshit
			^[\lin, 0.0001, 1];
		}{
			^known[paramName]
		};
	}

	destroy{
		//StormServer.getStormGUI.destroyGUI(this);
	}

}
