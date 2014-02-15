StormPattern  {
	var <>scale,<>synth,<>pattern,<>track;

	*new {
		|synthlab|
		^super.new.init(synthlab);
    }

	init{
		|synthlab|
		var params,key = false;
		synth = synthlab;
		track = StormServer.getSequencerTrack();
		params = synth.getParamsArray;
		params.do({
			|value|
			if (key == false,{
				key = value;
			},{
					track.set(key,value);
					key = false;
			});
		});
		track.set(\instrument,synth.name);
		track.set(\freq,Pseq(Scale.choose.degrees+150,inf));

		//params.add(track);
		//Pbind(*params).play;
	}


}
