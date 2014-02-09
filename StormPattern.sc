StormPattern  {
	var <>scale,<>synth,<>pattern;

	*new {
		|synthlab|
		^super.new.init(synthlab);
    }

	init{
		|synthlab|
		var params;
		synth = synthlab;
		params = synth.getParamsArray;
		params.add(\instrument);
		params.add(synth.name);
		params.add(\freq);
		params.add(Pseq([60.midicps],inf));
		Pbind(*params).play;
	}


}
