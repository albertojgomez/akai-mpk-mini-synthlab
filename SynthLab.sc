SynthLab  {
	var <>sdef,<>controlEvent,<>notematrix,<>gui,<>buses,<>midiChannel,seed,<>knobs,
	    <>panels,mainWindow,<>activePanel;
	*new {
		|name,graphFunc|
		^super.new.init(name,graphFunc);
    }

	init{
		|name,graphFunc|
		//SET MIDI DEVICE
		var srcID;
		this.initMidiResources();
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
			if (Server.default.pid.isNil,{
				Server.default.options.memSize = 2.pow(20);
				Server.default.options.outDevice="Built-in Output";
				Server.default.options.sampleRate=44100;
				Server.default.options.numOutputBusChannels = 64;
				Server.default.bootSync;
				srcID = MIDIIn.findPort("IAC Driver", "Bus 1").uid;
			});
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
			Server.default.sync;
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
		MIDIIn.connectAll;
		//Notes
		MIDIFunc.noteOn({
		arg ...args;
		var note,params;
			note = args[1] ;
			params = this.getParamsArray();
			params.add(\freq);
			params.add(note.midicps);
			if (notematrix[ note ].notNil,{notematrix[ note ].release});
			notematrix[ note ] = Synth(name,params);

		},chan:midiChannel,srcID:srcID);
		srcID.postln;
		MIDIFunc.noteOff({
		arg ...args;
		var note;
			note = args[1] ;
			notematrix[ note ].release;

		},chan:midiChannel,srcID:srcID);
		//Knobs
		MIDIFunc.cc({
			|value,ccNumber|
			{
				var knobIndex;
				knobIndex = (activePanel * 8) + ccNumber - 1;
				if (knobs.at(knobIndex).notNil,{
					knobs.at(knobIndex).valueAction_( value / 127);
				});
			}.defer;
		},chan:midiChannel,srcID:srcID);
		MIDIFunc.noteOn({
			|velocity,note|
			if(panels.at(note).notNil,{
				this.setActivePanel(note);
			});
		},chan:midiChannel + 1,srcID:srcID);
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
	/*handles midi channels for synth*/
	initMidiResources{
		if(~midiChans.isNil,{~midiChans = [];~midiCounter = 0});
		//we only choose even numbers cos we leave odds for pads o same program
		midiChannel = ~midiCounter * 2;
		~midiCounter = ~midiCounter + 1;
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
