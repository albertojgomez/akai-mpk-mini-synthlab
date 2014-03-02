StormPattern  {
	var <>scale,<>synth,<>pattern,<>track,
	<>length,<>density,<>noterange,<>seed,<>linearity,<>root,<>chaos;

	*new {
		|synthlab|
		^super.new.init(synthlab);
    }

	init{
		|synthlab|
		var params,key = false;
		synth = synthlab;
		track = StormServer.getSequencerTrack();
		track.postln;
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
		length = 16;
		density = 50;
		noterange = 15;
		linearity = 25;
		chaos = 5;
		root = 35;
		scale = Scale.choose.degrees;
		track.set(\instrument,synth.name);
		track.set(\freq,Pseq([\rest],inf));
		^this;
	}

	mutate{
		StormServer.getClock.schedAbs(StormServer.getClock.nextTimeOnGrid(length), {
			track.set(\freq,Pseq(this.createPattern.midicps,inf));
		});
	}

	createPattern{
		var noteArray = Array.new(noterange),pointer;
		noterange.do({
			|count|
			count = count - (noterange/2).floor;
			noteArray.add(root + ((count / 7).floor * 12) + scale[count%7]);
		});
		pointer = (noterange/2).floor;
		pattern = Array.new(length);
		length.do({
			|step|
				var note;
			if ((density/100).coin,
			{
					if ((linearity/100).coin,{
					var steps = [0];
						if(noteArray[pointer-1].isNil.not,{
							steps.add(-1);
						});
						if(noteArray[pointer+1].isNil.not,{
							steps.add(1);
						});
						pointer = pointer + steps.choose;
						note = noteArray[pointer];
				},{
						var jumprange1,jumprange2;
							if (pointer - (chaos/2).floor < 0,{
								jumprange1 = 0;
							},{
								jumprange1 = pointer - (chaos/2).floor;
							});
						if (pointer + (chaos/2).floor >= noteArray.size,{
								jumprange2 = noteArray.size -1;
							},{
								jumprange2 = pointer + (chaos/2).floor;
							});
							note = noteArray.copyRange(jumprange1.asInteger,jumprange2.asInteger)
								.choose.asInt;
							pointer = noteArray.indexOf(note.asFloat);
				});
			},
			{
				note = \rest;
			});
			pattern.add(note);
		});
		^pattern;
	}


}
