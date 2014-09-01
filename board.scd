StormSynth.allSequencers.size
(
a = StormSynth({
	|freq|
	Pulse.ar(freq * SinOsc.kr(freq).range(0.95,1.05));
},\PW)
)

(
a = StormSynth({
	|freq|
	LFTri.ar(freq*10 * SinOsc.kr(freq).range(0.95,1.05));
},\PWw)
)
StormSynth.group
"Sounds".resolveRelative
PathName("Sounds".resolveRelative).filesDo({|i| i.fileName.postln})

Path
w=a.controls.keys.asArray
*w
a.buses[]
a.sequencer]
a.score.source = Pseq([40],inf);
a.sequencer.play
a.noteOn(30)
a.noteOff(30)
a.noteMatrix[81].release
StormServer.clock.tempo=5
(
e = (\instrument:\PW);
a.buses.forEach({
	|value, key|

	e[key] = value.asMap;

});



	e.play;

	e.postln;
)

)
a.buses['vol'].set(1);
z=[]
Pbind(*w);
a.release
(
a = SynthDef(\LEL, {
	|freq = 400|
	s = SynthDef.wrap({
		SinOsc.ar(freq)
	});
	Out.ar(1,s);
}).add

)
trace
SynthDescLib.global.synthDescs[\PW].controls[0].defaultValue

SynthDescLib.default.synthDescs.do({|s|s.name.postln})