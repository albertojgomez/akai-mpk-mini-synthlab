(
SynthDef("bassdrum",{
	|decay=0.5,vol = 1,sig|
	var env=EnvGen.kr(Env.perc(0,releaseTime:decay,curve:-18),Impulse.kr(0)  , doneAction:2);
	sig = [
		SinOsc.ar(env.range(20,60),mul:env*vol),
		Saw.ar(env.range(20,60),mul:env*0.01)
	];
	Out.ar([0,1],Mix(sig));

	}).add;

/////////////snare
SynthDef("snare", {
	arg decaynoise=0.25,decay2=0.1,vol=1,out=[2,3];
	var trigger,env1,env2,amp,o1,o2,n,freq;
	o1=FSinOsc.ar(236);
	o2=FSinOsc.ar(168);
	w= WhiteNoise.ar(mul:0.5).clip(0,1);
	n =w+ Mix.fill(8,{|i| AllpassC.ar( w,  0.005*i, 0.005*i , 0,1/8)});

	trigger=Impulse.kr(0);
	env1=Env.adsr( attackTime:0,decayTime:0.2,sustainLevel:1,releaseTime:decaynoise);
	env2=Env.perc(0,decay2);
	freq=EnvGen.kr(env2,gate:trigger );
	amp=EnvGen.kr(env1,gate:trigger  , doneAction:2);

	a=RHPF.ar(Mix([o1,o2]),600,0.2,0.7);

	b=a*freq*vol*1;

	d=n*amp;
	c=Mix([RLPF.ar(d,XLine.kr(2000,100,decaynoise),2,mul:vol/2),
	RHPF.ar(d,10000,0.3,mul:vol/2)]);

	Out.ar([0,1] ,Mix([c,b])*0.5)
}).add;
)
MIDIIn.connectAll;
MIDIFunc.noteOn({
		arg ...args;
		var note,params;
	Synth("snare");
		},chan:4);
MIDIFunc.noteOn({
		arg ...args;
		var note,params;
	Synth("bassdrum");
		},chan:5);

Synth("bassdrum",[\decay:0.5])
Synth("snare",[\decaynoise,0.5])


ServerOptions.outDevices
ServerOptions.devices
Server.default.options.outDevice
MIDIFunc.trace
StormServer.singleton.midiDevice

MIDIClient.sources[0].uid
Server.default.doWhenBooted({'s'.post})
Scale.choose.degrees+1500
TempoClock.default.tempo = 115*4/60;

{ LPF.ar( Ringz.ar( Impulse.ar(1), 60, 0.5 ), 500 ) * 0.25 }.play;
a = {
        var sig = LPF.ar( Ringz.ar( Impulse.ar(1), 40, 0.5 ), 500 ),
                cmp = CompanderD.ar(sig, thresh: -20.dbamp, slopeBelow: 1, slopeAbove: 0.3, clampTime: 0.003, relaxTime: 0.08);
        (cmp * (10.dbamp )) ! 2
}.play; 