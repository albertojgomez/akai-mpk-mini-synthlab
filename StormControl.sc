/* Only things a SC must know is the bus and it's name*/
StormControl : StormObject{
	classvar width = 60 , height = 60, <>controls;
	var <>name, <>bus;
	*initClass{
		controls = ();
	}

	*new {
		|cName, cBus|
		if (cName.isNil | cbus.cBus) {
			Exception("StormControl:Control name or bus not provided");
		}
		^super.new.initStormControl(cName, cBus);
    }

	initStormSynth{
		|cName,cBus|
		bus = cBus;
		name = cName;
	}

}