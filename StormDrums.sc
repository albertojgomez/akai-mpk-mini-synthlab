
StormDrums : StormObject{
	classvar width = 640 , height = 125;
	var <>name, <>object, value, <>controlSpec, <>knob, <>text;

	*initClass{

	}

	*new {
		^super.new.initStormDrums();
    }

	initStormDrums{
		[\BDshort,\BD,\RS,\SD,\CPshort,\CP,\CB,\CH,\CL,\CH,\LT,\OH,\MT,\MT,\CY,\HT].do({
			|inst, index|

		});




	}

}

/*
[1,2,3].do({|i| i.post});
Storm Sequencer
-turing (+ euclidean for synth pitches)

EUC
-length
-fills
-rotation

TUR

-Change prob
-scale



extra
-seed for initial pattern?
-note range
*/