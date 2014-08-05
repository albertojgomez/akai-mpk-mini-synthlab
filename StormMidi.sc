StormMidi  {
	var <>midiInterface, <>midiOut, <>midiIn, <>connectTo,<>midiInstruments;

	*initialize{
		//TODO change everything to static (only one stormmidi needed)
		//heavy use of class var

	}

	*new {
		^super.new.init;
    }
	init{
		var inDevice;
		MIDIClient.init;
		MIDIIn.connectAll;
		midiOut = MIDIOut.newByName("IAC Driver", "Bus 1");
		inDevice = MIDIIn.findPort("MPK mini", "MPK mini");
		if (inDevice.isNil,{
			midiIn = 0;
		},{
			midiIn = MIDIIn.findPort("MPK mini", "MPK mini").uid;
		});

		connectTo = MIDIIn.findPort("IAC Driver", "Bus 1").uid;
		midiOut.latency = 0;
		midiInstruments = ();
		^this
	}



	connectSynth {
		|stormSynth|
		var currentIndex, inputCCchannel,inputSelectorchannel,
		    outputchannel,pointer,midiout,responder;
		if (midiInstruments[stormSynth.name].isNil.not,
		//existed
		{
			this.disconnectMidi(stormSynth);
			currentIndex = midiInstruments[stormSynth.name][\index];
			inputSelectorchannel = currentIndex * 2 + 1;
			inputCCchannel = currentIndex * 2;
			outputchannel = currentIndex * 2;
		},
		//did not exist
		{
			currentIndex = midiInstruments.size;
			midiInstruments.[stormSynth.name] = (
				\stormsynth:stormSynth,
				\midiResponders : List.new(),
				\index : currentIndex,
				\notematrix : ()
			);
			inputSelectorchannel = currentIndex * 2 + 1;
			inputCCchannel = currentIndex * 2;
			outputchannel = currentIndex * 2;
			pointer=0;
			//cc group selector
			MIDIFunc.noteOn({
				|velocity,note|
				pointer = note;
			},chan:inputSelectorchannel,srcID:midiIn);
			//cc multiplex
			MIDIFunc.cc({
				|value,ccNumber|
				var  ccId;
				ccId = (pointer*8) + ccNumber;
				midiOut.control(outputchannel,ccId,value)
			},chan:inputCCchannel,srcID:midiIn);
			//Bypass noteon and  noteoff
			MIDIFunc.noteOn({
				|velocity,note|
				midiOut.noteOn(inputCCchannel, note: note, veloc: velocity)
			},chan:inputCCchannel,srcID:midiIn);
			MIDIFunc.noteOff({
				|velocity,note|
				midiOut.noteOff(inputCCchannel, note: note, veloc: velocity)
			},chan:inputCCchannel,srcID:midiIn);

		});

		//Knobs
		responder = MIDIFunc.cc({
			|value,ccNumber|
			{
				var knobs;
				knobs = StormServer.getStormGUI.instrumentGUIs[stormSynth.name][\knobsList];
				//-1 cos the ccs start at 1
				if (knobs.at(ccNumber - 1).notNil,{
					var kval =  value,currVal = knobs.at(ccNumber - 1).value * 127;
					if ((currVal - kval).abs < 5){
						knobs.at(ccNumber - 1).valueAction_(kval / 127 );
					}
				});
			}.defer;
		},chan:outputchannel,srcID:connectTo);
		midiInstruments[stormSynth.name][\midiResponders].add(responder);
		//notes
		responder = MIDIFunc.noteOn({
			arg ...args;
			var note,params;
			note = args[1] ;
			params = stormSynth.getParamsArray();
			params.add(\freq);
			params.add(note.midicps);
			midiInstruments[stormSynth.name][\notematrix][ note ] = Synth(stormSynth.name,params);
		},chan:inputCCchannel,srcID:connectTo);
		midiInstruments[stormSynth.name][\midiResponders].add(responder);
		responder = MIDIFunc.noteOff({
			arg ...args;
			var note;
			note = args[1] ;
			midiInstruments[stormSynth.name][\notematrix][ note ].release;
		},chan:inputCCchannel,srcID:connectTo);
		midiInstruments[stormSynth.name][\midiResponders].add(responder);
	}

	disconnectMidi{
		|stormSynth|
		midiInstruments[stormSynth.name][\midiResponders].do({
			|midiFunc| midiFunc.free;
		});
		midiInstruments[stormSynth.name][\midiResponders] = List.new();
		midiInstruments[stormSynth.name][\notematrix].do({|note|
			if(note.isNil.not,{note.release});
		});
		midiInstruments[stormSynth.name][\notematrix] = ();
	}
}

