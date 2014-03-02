StormMidi  {
	var <>midiInterface, <>midiOut, <>midiIn,<>instruments, <>connectTo;
	*new {
		^super.new.init;
    }

	init{
		MIDIClient.init;
		MIDIIn.connectAll;
		midiOut = MIDIOut.newByName("IAC Driver", "Bus 1");
		midiIn = MIDIIn.findPort("MPK mini", "MPK mini").uid;
		connectTo = MIDIIn.findPort("IAC Driver", "Bus 1").uid;
		midiOut.latency = 0;
		instruments = Array.new(4);
		^this
	}

	patch {
		|synthlab|
		var currentIndex, inputCCchannel,inputSelectorchannel,outputchannel,pointer,
		midiout,inputDevice,outputDevice;
		currentIndex = instruments.size;
		inputSelectorchannel = currentIndex * 2 + 1;
		inputCCchannel = currentIndex * 2;
		outputchannel = currentIndex * 2;
		pointer=0;
		instruments.add(synthlab);
		inputDevice =  midiIn;
		outputDevice = midiOut;

		MIDIFunc.noteOn({
			|velocity,note|
			pointer = note;
		},chan:inputSelectorchannel,srcID:inputDevice);

		MIDIFunc.cc({
			|value,ccNumber|
			var  ccId;
			ccId = (pointer*8) + ccNumber;
			outputDevice.control(outputchannel,ccId,value)
		},chan:inputCCchannel,srcID:inputDevice);

		//Bypass noteon and  noteoff
		MIDIFunc.noteOn({
			|velocity,note|
			outputDevice.noteOn(inputCCchannel, note: note, veloc: velocity)
		},chan:inputCCchannel,srcID:inputDevice);

		MIDIFunc.noteOff({
			|velocity,note|
			outputDevice.noteOff(inputCCchannel, note: note, veloc: velocity)
		},chan:inputCCchannel,srcID:inputDevice);

	}




}

/*
Array.new(4).add(1)
