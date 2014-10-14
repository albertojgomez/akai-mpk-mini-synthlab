ScaleInfo.names
Pseries
(
p = Pbind(
        // making 'n' a separate stream so that degree and dur can share it

    \degree, Pstutter(Pseq(Scale.choose.postln.degrees.reverse ,\inf), Pwhite(-4, 11, inf),inf),
	\dur, Pstutter(Pseq([40,60,20],\inf), Pwhite(2, 6, inf),inf),
    \legato, 0.3,
\instrument, \PW
).play;
)

Pbrown
(
a = StormSynth({
	|freq|
	Pulse.ar(freq * 2 * SinOsc.kr(freq/4).range(0.95,1.05));
},\PW)
)

(
a = StormSynth({
	|freq|
	LFTri.ar(freq*10 * SinOsc.kr(freq).range(0.95,1.05));
},\PW)
)
StormServer.clock.tempo=1

label
Knob(a.controls[\filter_cutoff].view,60@60)
a.buses[\filter_reso].getSynchronous
a.buses[\filter_reso].set(4)
a.sequencer]
a.score.source = Pseq(Scale.choose.postln.degrees.postln ++ [\rest,\rest]  + 40,inf);
a.score.source = Pseq([\rest],inf);
a.sequencer.play
a.noteOn(30)
a.noteOff(30)
a.noteMatrix[81].release
StormServer.clock.tempo=1

