Drumpart  {

	var <>view,<>win,<>slider,<>params,<>pattern,<>pproxy,<>inst,<>labels,<>button1,<>button2;
	*new {
		|parent,instrument|
		^super.new.init(parent,instrument);
    }
	init{
		|parent,instrument|
		//set pareant graph
		win = parent ;
		labels = ();
		//gui elements
		view = View.new(win, Rect(0, 0, 140, 500));

		slider = Slider(view, Rect(30,80,30,300)).action_({
			|sl|
			this.sliderResponse(sl);

		}).value(0.5);
		params = List.new;
		//add synth params
		inst = instrument ;
		this.addSynthParams(instrument);
		//add sequencer params
		this.addSequencerParams();
		//add labels
		this.addParamLabels();
		//set gui defaults
		this.initGui();
		//add buttons
		this.addButtons();
		//create pattern
		pattern = this.createAutomata();
		pproxy = PatternProxy(pattern);

		^this;
	}
	initGui {
		params.do({
			|p|
			p.knob.value = p.cSpec.unmap( p.cSpec.default );

		});


	}
	sliderResponse {
		|i|
		var ind, selected ;
		ind = (1 - i.value()*params.size).round - 1 ;
		if (ind<0,{ind=0});
		labels.do({
			|l|
			l.background= Color.new255(210,210,210);

		});
		params.do({
		|p|
			p.knob.visible = false;
			p.marker.visible = false;
		});
		selected = labels[ind.asInteger];

		this.paramByName(selected.string).knob.visible = true;
		this.paramByName(selected.string).marker.visible = true;
		selected.background = Color.red;


	}
	addButtons {
		button1 = Button(view,Rect(0,120,30,30)).action_({
			var clock;
			clock = TempoClock.default;
			clock.schedAbs(clock.nextTimeOnGrid(this.paramByName(\length)),
			{
					pattern = this.createAutomata();
			});
		});
		button2 = Button(view,Rect(0,300,30,30)).action_({
			\lop.postln;
		});
	}
	addParamLabels {
		var margin,n;
		n = params.size;
		margin =((300 - (n*20))/(n))+20;
		params.do({
			|elem,i|
			var y,t;
			y = 80;
			t = StaticText(view,
				Rect(60,y+(margin*i) ,80,20)).string_(elem.name).font_(Font("Helvetica",11));
			labels[i] = t;

		})

	}

	addSynthParams{
		|synthname|
		SynthDescLib.global.synthDescs.at(synthname).controls.do({
			|control|
			if ( control.name.asString.beginsWith("freq"),
			{ 	this.makeParam(control.name,control.defaultValue,0.0001,20000,true) },
			{  this.makeParam(control.name,control.defaultValue,0.0001,20000) }
			);
		});
	}

	addSequencerParams {
		this.makeParam(\complexity,4,1,8,false,1);
		this.makeParam(\length,16,1,64,false,16);
		this.makeParam(\density,0.2,0,1,false,0);
		this.makeParam(\mutagen,0.1,0,1,false,0);
		this.makeParam(\seed,0,0,127,false,1);
	}

	makeParam {
		|name,val,min,max,log=false,step=0|
		var k,warp,cspec;
		if (log, {warp=\exp} , {warp=\lin} );
		k = Knob(view,Rect(0, 20, 60, 60)).visible_(false);
		cspec = ControlSpec(min,max,warp,step,val);
		params.add ( (
			\cSpec : cspec,
			\knob:  k,
			\marker: StaticText(view, Rect(0, 0, 60, 20)).string_(val).visible_(false),
			\name: name
		) );
		k.action={
			|kn|
			var par,val;
			par = this.paramByName(name);
			val = par.cSpec.map(kn.value) ;
			par.cSpec.default = val ;
			par.marker.string = val;
		}
	}

	createAutomata  {
		var base,patterns ,
		states = [
			[0]
		] ,density;
		density = this.paramByName(\density).cSpec.default ;
		thisThread.randSeed = this.paramByName(\seed).cSpec.default;
		base = Pwrand(
			[0,1],
			[1- density,density ].normalizeSum,
			inf).asStream.nextN(this.paramByName(\length).cSpec.default);
		patterns = Array.fill( this.paramByName(\complexity).cSpec.default, {
			this.mutatePattern.(base,this.paramByName(\mutagen).cSpec.default)
		}) ;

		this.paramByName(\complexity).cSpec.default.do({
			|i|
			states = states.add(Pseq(patterns[i]) );
			states = states.add(Array.fill(i+2,{
				|j|
				if (this.paramByName(\complexity).cSpec.default == j ,0,j);
			}));
		});
		^Pfsm(states);
	}

	mutatePattern  {
		arg
		pattern=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
		mutagen=0.1;//0-1
		var copy;
		copy = pattern.copy;
		^copy.do({
			|item,index|
			if (mutagen.coin,{
				copy.put(index,item.asBoolean.not.asInt);
			});
		});
	}

	paramByName{
		|name|
		params.do({
			|elem,index|
			if(elem.name.asSymbol == name.asSymbol,{^elem});
		});
		^nil;
	}

	getPBind {
		var pbindPairs,controls;
		controls = SynthDescLib.global.synthDescs.at(inst).controls;
		pbindPairs = List.new(controls.size+4);
		pbindPairs.add(\instrument);
		pbindPairs.add(inst);
		pbindPairs.add(\vol);
		pbindPairs.add(pproxy);
		SynthDescLib.global.synthDescs.at(inst).controls.do({
			|control|
			if (control.name !='vol',{
				pbindPairs.add(control.name.asSymbol);
				pbindPairs.add( Pfunc( { this.paramByName(control.name).cSpec.default } ) );
			});
		});
		^pbindPairs.asArray;
	}





}