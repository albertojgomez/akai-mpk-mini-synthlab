(
p = ProxySpace.new();

/******** MIXER ************/
~numchannels = 10;
~channels = Array.new(~numchannels);
~numchannels.do({
	|i|
	~channels.add(Ndef(("chan" ++ i).asSymbol,{ 0 }));
});

Ndef(\mix,{
	var mix = Array.new(~numchannels);
	~channels.do({
		|i|
		mix.add(i.ar);
		i.fadeTime_(5);
	}) ;
	Mix(mix);
});

)
TempoClock.default.tempo=110/60 *4
Ndef(\out,{ Out.ar([0,1],Ndef(\mix).ar)  });



Ndef(\chan0,{ Dust2.ar(LFNoise0.kr(1/5).range(0.5,3)) * 0.2 })
Ndef(\chan1,{ BrownNoise.ar * 0.01 });
Ndef(\chan2,{ CombN.ar( Splay.ar([Ndef(\chan0).ar, Ndef(\chan3).ar],0.5) )* 0.2 });
Ndef(\chan3,{ Ringz.ar(Ndef(\chan0).ar + Ndef(\chan4).ar,LFNoise0.kr.range(400,500)) * 0.05 })
Ndef(\chan4, { Mix.arFill(10,{|i| LFSaw.ar( 60 + i ) }) * 0.0003});


MIDIClient.init;
m = MIDIOut(0);

a = Pbind(\degree, Prand([1, 2, 3, [0, 5]], inf), \bend, Pwhite(0, 76, inf));


// chain a midi event into the pattern and play it (see Pchain)

(a <> (type: \midi, midiout: m)).play;