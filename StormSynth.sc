StormSynth : StormObject  {
	classvar width = 640 , height = 125, <>synths, <>group, <>allSequencers;
	var <>controls, <>buses, <>name, <>graphFunction, <>noteMatrix, <>sequencer;

	*initialize{
		group = Group.new(StormServer.scServer,\addToTail);
		//add enogh sequencers
		allSequencers = List.new(20);
		20.do({
			|i|
			allSequencers.add(PbindProxy.new.set(\freq,\rest).set(\group,group));
		});
		StormServer.clock.schedAbs(StormServer.clock.nextTimeOnGrid(64), {
			Ppar(allSequencers).play(quant:4)
		});
		synths = ();
	}

	*new {
		|graphFunc, synthName, filter|
		if (synthName.isNil){
			synthName = "StormSynth_" ++ synths.size;
		};
		^super.new.initStormSynth(synthName, graphFunc, filter);
    }

	initStormSynth{
		|synthName, graphFunc, filter|
		var prevParameterValues, prevSynth;
		name = synthName;
		graphFunction = graphFunc;
		buses = ();
		controls = ();
		noteMatrix = ();
		//if synth already existd, backup parameters to restore later
		prevSynth = synths[synthName];
		if(prevSynth.isNil.not){
			//get controls
			prevParameterValues = ();
			controls.forEach({
				|control, name|
				prevParameterValues[name] = control.getValue();
			});
			prevSynth.destroy();
		}{
			sequencer = allSequencers.pop();
		};
		//wrap in routine so that we can sync with server and get controls
		{
			if (filter.isNil){
				filter = MoogFF;
			};
			//do synth def
			SynthDef( name , {
				| gate = 1, env_attack = 0.5, env_decay  = 0.5, env_sustain = 1,
				env_release = 1,filter_attack = 0.5, filter_decay  = 0.5,
				filter_sustain = 1,filter_release  = 1,
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
				signal = MoogFF.ar(signal,filter,filter_reso);
				signal = signal * amp * vol;
				Out.ar( [0,1]  , signal );
				DetectSilence.ar(signal,doneAction:2);
			} ).add();
			StormServer.scServer.sync;
			this.createControls();
			//restore previous values if there are
			if (prevParameterValues.isNil.not){
				prevParameterValues.forEach({
					|value, key|
					if (controls[key].isNil.not){
						controls[key].setValue(value);
					}
				});
			};
		}.fork;
		synths[name] = this;
		^this;
	}

	createControls{
		SynthDescLib.global.synthDescs.at(name).controls.do({
			|control|
			var exclude = [\freq , \gate];
			if ( [\freq , \gate].includesEqual(control.name).not ){
				buses[ control.name ] = Bus.control.set(control.defaultValue);
				sequencer.set(control.name, buses[ control.name ].asMap);
				controls[control.name] = StormControl.new(
					control.name, buses[control.name], view);
			};
		});
	}

	/*Return argument pairs list to use with Synth()*/
	noteOn {
		|midinote = 40|
		var e;
		if (noteMatrix[midinote].inNil){
			e = List[] ;
			controls.do({
				|contr|
				e.add( contr.name );
				e.add( buses[ contr.name ].asMap );
			});
			e.add(\freq);
			e.add(midinote.midicps);
			^noteMatrix[midinote] = Synth(name,e);
		};
	}

	noteOff {
		|midinote = 40|
		var e;
		if (noteMatrix[midinote].inNil.not){
			noteMatrix[midinote].release();
		};
	}

	destroy{
		//TODO destroy buses!
		buses.forEach({
			|value, key|
			value.free();
		});
	}

}
