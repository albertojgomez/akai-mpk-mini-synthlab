SynthLab  {
	var <>sdef,<>controlEvent,<>notematrix,<>gui,<>buses,<>midiChannel,<>midiCCSelectChannel,seed,<>knobs,
	    <>panels,mainWindow,<>activePanel,<>name;
	*new {
		|nameparam,graphFunc|
		^super.new.init(nameparam,graphFunc);
    }

	init{
		|nameparam,graphFunc|
		name = nameparam;
		//init server
		StormServer.singleton;
		Server.default.doWhenBooted({
			//SET MIDI DEVICE
			midiChannel = StormServer.getMidiChannel;
			midiCCSelectChannel = StormServer.getMidiChannel;
			controlEvent = ();
			seed = rrand(0, 100);
			buses = () ;
			knobs = Array.new(128) ;
			panels = Array.new(16) ;
			gui = Window.new(
				name ++ " channel " ++ midiChannel, 1000@500).front;//.background_(Color.black);
			gui.view.decorator = FlowLayout( gui.view.bounds, 10@10, 5@5 );
			//wrap in routine so that we can sync with server and get controls
			{

				var controlIndex=0,
				exclude = [\freq , \gate];
				//do synth def
				sdef = SynthDef( name , {
					| gate = 1,
					env_attack = 0.5,
					env_decay  = 0.5,
					env_sustain = 1,
					env_release = 1,
					filter_attack = 0.5,
					filter_decay  = 0.5, filter_sustain = 1,
					filter_release  = 1,
					filter_cutoff=10000, filter_reso=3 ,vol = 1|
					var signal,env,amp,fenv,filter;
					//amp env
					env=Env.adsr(
						attackTime:env_attack,
						decayTime:env_decay,
						sustainLevel:env_sustain,
						releaseTime:env_release );
					amp=EnvGen.kr(env, gate, doneAction:2);
					//filter env
					fenv=Env.adsr(
						attackTime:filter_attack,
						decayTime:filter_decay,
						sustainLevel:filter_sustain,
						releaseTime:filter_release );
					filter=EnvGen.kr(fenv,gate:gate).exprange(50,filter_cutoff);

					signal = SynthDef.wrap(graphFunc);

					signal = MoogFF.ar( signal,filter,filter_reso,0) ;
					//ATTENTION
					//sometimes a note off will got lost and a synth will get stuck
					//cut it anyway after 30 secs
					Line.kr(0,1,30,doneAction:2);
					Out.ar( [0,1]  , signal * amp * vol );
				} ).add;
				StormServer.sync;
				SynthDescLib.global.synthDescs.at(name).controls.do({
					|control|
					if ( exclude.indexOf( control.name ).isNil ,{
						controlEvent[ control.name ]=this.makecSpec(control,controlIndex);
						buses [ control.name ] = Bus.control.set(
							controlEvent[ control.name ].default
						);
						controlIndex = controlIndex+1;
					});
				});
				this.setActivePanel();
			}.fork;
			//bind MIDI stuff
			notematrix = ();
			StormServer.connectMidi(this);

		});//END OF CODE RUN whith doWhenBooted


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
	/*Return ControlSpec to handle a given Synth parameter*/
	/*Also creates knob*/
	makecSpec{
		|control,index|
		var k ,cspec, label, group, color, specData, nameParts, name;
		nameParts = control.name.asString.split($_);
		if (nameParts.size == 2 ,{
			group = nameParts[0]  + seed;
			name = nameParts[1];
		},{
			name = control.name.asString;
			group = 'randomString' ++ seed;
		});
		color = SynthLab.makeColor(group) ;
		specData = SynthLab.paramTypes( name.asSymbol );
		cspec = ControlSpec(
			specData[1] ,  specData[2]  ,
			specData[0] , 0 ,  control.defaultValue  ,control.name
		);

		{
			var view,s = 0.8, tempPanel;
			if (index % 8 == 0,{
				tempPanel = View(gui.view,Rect(0,0,60*s*4,60*s*2)
					);
				panels = panels.add(tempPanel);

			},{
				tempPanel = panels.at((index/8).floor);
			});
			//add label
			view = View(
				tempPanel ,
				Rect( 60 *  (index % 4 )  *s ,
					60  * ( (index % 8) / 4 ).floor * s ,
					55 * s ,
					55 * s )
			);
			label = StaticText(
				view ,Rect(0,30 * s,60 * s,20))
			.string_( name ).align_(\center).font_(
				Font("Monaco", 11 * s)
			);
			//init knob with value
			k = Knob(view,Rect( 15 * s , 5 * s , 30 * s  , 30 * s )).mode_(\vert);
			k.value = cspec.unmap( cspec.default );
			//add action
			k.action={
				|kn|
				var par,val;
				par = controlEvent[control.name.asSymbol];
				val =par.map(kn.value) ;
				par.default = val ;
				buses[ control.name ].set(val)
			};
			knobs = knobs.add(k);
			view.background = color;
		}.defer;
		^cspec;
	}

	//must be called once we are sure the panels exist
	setActivePanel{
		|panelIndex = 0|
		{
			activePanel = panelIndex;
			panels.do({
				|pan|
				pan.background_(Color.new(0,0,0,0));
			});
			panels.at(activePanel).background_(Color.green);
		}.defer;
	}
	/*Returns warp, min and max of a parameter with a given name.Defaults to \lin,0,1*/
	*paramTypes{
		|paramName|
		var known = (
			\freq : [\exp, 20, 20500],
			\cutoff : [\exp, 20, 20500],
			\attack : [\lin, 0, 10] ,
			\decay : [\lin, 0, 10] ,
			\release : [\lin, 0, 10]
		);
		if (known[paramName].isNil,
			{
				^[\lin, 0, 1];
			},
			{
				^known[paramName]
		    });
	}
	*makeColor{
		|group|
		var hash,r,g,b;
		hash = abs(group.hash).asString;
		r= hash.at(0)++hash.at(1);
		g= hash.at(2)++hash.at(3);
		b= hash.at(4)++hash.at(5);
		^ Color.new(r.asFloat/100,g.asFloat/100,b.asFloat/100,0.5);
	}


}
