



//TODO
/*
Bypass keyboard notes
*/
(
var inputDevice,outputDevice,inputCCchannel,inputSelectorchannel,outputchannel,pointer,midiout;

MIDIClient.init;
MIDIIn.connectAll;
inputSelectorchannel = 1;
inputCCchannel = 0;
outputchannel = 0;
pointer=0;

inputDevice =  MIDIIn.findPort("MPK mini", "MPK mini").uid;
//outputDevice =  MIDIIn.findPort("IAC Driver", "Bus 1").uid;
outputDevice = MIDIOut.newByName("IAC Driver", "Bus 1");
outputDevice.latency = 0;

MIDIFunc.noteOn(
	{
			|velocity,note|
			pointer = note;
	},chan:inputSelectorchannel,srcID:inputDevice);

MIDIFunc.cc(
	{
		|value,ccNumber|
		var  ccId;
		ccId = (pointer*8) + ccNumber;
		ccId.postln;
		outputDevice.control(outputchannel,ccId,value)
	},chan:inputCCchannel,srcID:inputDevice);

//Bypass noteon and  noteoff
MIDIFunc.noteOn(
	{
		|velocity,note|
		outputDevice.noteOn(inputCCchannel, note: note, veloc: velocity)
	},chan:inputCCchannel,srcID:inputDevice);

MIDIFunc.noteOff(
	{
		|velocity,note|
		outputDevice.noteOff(inputCCchannel, note: note, veloc: velocity)
	},chan:inputCCchannel,srcID:inputDevice);

)
MIDIClient.sources
MIDIFunc.trace