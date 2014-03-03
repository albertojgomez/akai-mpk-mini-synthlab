StormGUI {
	var <>guiSize,<>mainWindow,<>instrumentGUIs;
	/* instruments will have 1 event per inst in the format
	(guis:array,container:view)

	*/
	*new {
		^super.new.init;
    }

	init{
		guiSize = 0.7;
		mainWindow = Window.new("St0rmB0tn3t", 1280@800).front;
		mainWindow.view.decorator = FlowLayout( mainWindow.view.bounds, 0@0, 0@0 );
		instrumentGUIs = ();
		^this;
	}

	/* create pseudo-random color based on a key */
	*makeColor{
		|group|
		var hash,r,g,b;
		hash = abs(group.hash).asString;
		r= hash.at(0)++hash.at(1);
		g= hash.at(2)++hash.at(3);
		b= hash.at(4)++hash.at(5);
		^Color.new(r.asFloat/100,g.asFloat/100,b.asFloat/100,0.5);
	}

	addSynth{
		|stormSynth|
		{
			if(instrumentGUIs[stormSynth.name].isNil.not,{
				this.destroyGUI(stormSynth);
			},
			{
				var instrumentContainer;
				instrumentContainer = View.new(mainWindow,1280@170);
				//separator
				View.new(mainWindow,1280@1).background_(Color.black);
				instrumentContainer.decorator
					= FlowLayout( instrumentContainer.bounds, 0@0, 0@0 );
				instrumentContainer;//.background_( Color.black );
				instrumentGUIs[stormSynth.name] = (
						\container : instrumentContainer,
						\guis : List.new(),
						//the knob list has the same elements but indexed
						\knobsList : List.new(),
						\knobs : (),
						\panels : Array.fill(16,{
							|panelIndex|
							var panel;
							panel = View.new(instrumentContainer,159@84);
							panel.decorator
								= FlowLayout( panel.bounds, 0@0, 0@0 );

						})
				);
			});
			//Create knobs
			stormSynth.paramNames.do({
				|key, keyIndex|
				var control = stormSynth.controlEvent[key],
					panel = instrumentGUIs[stormSynth.name].panels[floor(keyIndex / 8)],
				knobContainer = View(panel,39@40),
				knob = Knob(knobContainer,Rect(8,0,25,25)).mode_(\vert),
				label,nameParts,name,group;
				nameParts = key.asString.split($_);
				if (nameParts.size == 2 ,
				{group = nameParts[0];name = nameParts[1]},
				{group = 'randomString';name = key.asString});
				knobContainer.background_(StormGUI.makeColor(group));
				label = StaticText(knobContainer, Rect(0,25,40,15)).
					string_( name).align_(\center).font_(Font("Monaco", 9));
				knob.value = control.unmap( control.default );
				//add action
				knob.action = {
					|kn|
					var par,val;
					par = control;
					val = par.map(kn.value) ;
					par.default = val ;
					stormSynth.buses[ control.units ].set(val)
				};
				instrumentGUIs[stormSynth.name][\knobs][key] = knob;
				instrumentGUIs[stormSynth.name][\knobsList].add(knob);
				instrumentGUIs[stormSynth.name][\guis].add(knobContainer);
			});

		}.defer;
	}

	/* To be called when an  instrument is redefined */
	destroyGUI{
		|stormSynth|
		//delete all elements
		instrumentGUIs[stormSynth.name][\knobs].do({
			|view| {view.remove}.defer;
		});
		instrumentGUIs[stormSynth.name][\guis].do({
			|view| {view.remove}.defer;
		});
		instrumentGUIs[stormSynth.name][\guis] = List.new();
		instrumentGUIs[stormSynth.name][\knobs] = ();
		instrumentGUIs[stormSynth.name][\knobsList] = List.new();
		instrumentGUIs[stormSynth.name].panels.do({|panel| panel.decorator.reset });
	}
}


