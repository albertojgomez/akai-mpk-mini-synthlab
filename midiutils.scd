



//TODO
/*
Bypass keyboard notes
Reconfigure MPK mini preset
*/
(
var input,output,inputchannel,outputchannel,pointer,midiout;
MIDIClient.init;
MIDIIn.connectAll;
inputchannel = 0;
outputchannel = 0;
pointer=0;
input =  MIDIIn.findPort("MPK mini", "MPK mini").uid;

output =  MIDIIn.findPort("IAC Driver", "Bus 1").uid;


midiout = MIDIOut.newByName("IAC Driver", "Bus 1");
midiout.latency = 0;

MIDIFunc.noteOn(
	{
			|velocity,note|
			pointer = note;
			pointer.postln;
		},chan:1,srcID:input);
MIDIFunc.cc(
	{
			|value,ccNumber|

			var  ccId;

		ccId = (pointer*8)+ccNumber;
		ccId.postln;
	midiout.control(outputchannel,ccId,value)
		},chan:inputchannel,srcID:input);
)
MIDIClient.sources
MIDIFunc.trace