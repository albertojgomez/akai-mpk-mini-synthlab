StormServer  {
	var <>sdef,<>controlEvent,<>notematrix,<>gui,<>buses,<>midiChannel,seed,<>knobs,
	    <>panels,mainWindow,<>activePanel;
	*new {
		^super.new.init;
    }

	init{
		^this;
	}
	/*Return argument pairs list to use with Synth()*/
	method{
		^Nil;
	}

	*staticMethod{
		^Nil;
	}


}
