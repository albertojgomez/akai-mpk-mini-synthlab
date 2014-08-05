/* Only things a SC must know is the bus and it's name*/
/* All controls ARE KNOBS*/
StormControl : StormObject{
	classvar width = 60 , height = 60, <>controls;
	var <>name, <>object;

	*initClass{
		controls = ();
	}

	*new {
		|cName, cObject|
		if (cName.isNil | cObject.isNil) {
			Exception("StormControl:Control name or object not provided");
		};
		if (cObject.class.name == \Bus){
			^super.new.initStormControlFromBus(cName, cObject);
		};
		if(/*cObject.class.name == \Pproxy or something...*/false){
			^super.new.initStormControlPproxy(cName, cObject);
		}

    }

	initStormControlFromBus{
		|cName,cBus|
		object = cBus;
		name = cName;
		^this;
	}

	initStormControlPproxy{
		|cName, cPproxy|
		/*TODO*/
		^this;
	}

}