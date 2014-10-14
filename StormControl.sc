/* Only things a SC must know is the bus and it's name*/
/* All controls ARE KNOBS*/
StormControl : StormObject{
	classvar width = 60 , height = 60, <>controls;
	var <>name, <>object, value, <>controlSpec, <>knob, <>text;

	*initClass{
		controls = ();
	}

	*new {
		|cName, cBus, cParentView|
		if (cName.isNil || cBus.isNil) {
			Exception("StormControl:Control name or Bus not provided");
		};
		^super.new(cParentView).initStormControl(cName, cBus, cParentView);
    }

	initStormControl{
		|cName,cBus, cParentView|
		object = cBus;
		name = cName;
		controlSpec = StormControl.makeControlSpec(name);
		{
			var textparts;
			knob = Knob(view,25@25);
			knob.mode = \vert;
			knob.value = controlSpec.unmap(object.getSynchronous);
			knob.action={
				|k|
				object.set(controlSpec.map(k.value));
			};
			text = StaticText(view,35@30)
			.string_(name.asString.replace("_","\n"))
				.font_(Font("Monaco", 10));

		}.defer;
		^this;
	}

	//with this we detach the value from the Bus
	getValue{
		^value;
	}

	setValue{
		|newValue|
		newValue.postln;
		value = controlSpec.map(newValue);
		object.set(value);
		knob.value = value;
		^value;
		//change gui (defer),bus,ControlSpec....
	}

	/* Return ControlSpec to handle a given Synth parameter */
	/* paramameters are usually group_name */
	*makeControlSpec{
		|controlName|
		var cspec, group, specData, nameParts, name;
		nameParts = controlName.asString.split($_);
		if (nameParts.size == 2 ,{
			group = nameParts[0];
			name = nameParts[1];
		},{
			name = controlName.asString;
			group = 'randomString';
		});
		//Get control type (log, lin, range)
		specData = StormControl.getParamType( name.asSymbol );
		cspec = ControlSpec(
			specData[0] ,  specData[1]  ,
			specData[2] , 0 ,  0  ,controlName
		);
		^cspec;
	}

	/* Returns warp, min and max of a parameter with a given name.Defaults to \lin,0,1 */
	*getParamType{
		|paramName|
		var known = (
			\freq : [20, 20500, \exp],
			\cutoff : [20, 20500, \exp],
			\attack : [0, 10, \lin] ,
			\decay : [0, 10, \lin ] ,
			\release : [0.0001, 10, \lin]
		);
		if (known[paramName].isNil){
			//zero in  some param make the whole thing go apeshit
			^[0.0001, 1, \lin];
		}{
			^known[paramName]
		};
	}

	getDimensions {
		^Point(width,height);
	}

	destroy{
		view.remove();
	}

}

