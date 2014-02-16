(
SynthDef("bassdrum",{
	|decay=0.5|
	var env=EnvGen.kr(Env.perc(0,releaseTime:decay),Impulse.kr(0)  , doneAction:2);
	Out.ar([0,1],SinOsc.ar(env.range(20,80),mul:env));

	}).add;

/////////////snare
SynthDef("snare", {
	arg decaynoise=0.2,decay2=0.05,vol=1,out=[2,3];
	var trigger,env1,env2,amp,o1,o2,n,freq;
	o1=FSinOsc.ar(336);
	o2=FSinOsc.ar(168);
	w= WhiteNoise.ar(mul:0.5).clip(0,1);
	n =w+ Mix.fill(8,{|i| AllpassC.ar( w,  0.005*i, 0.005*i , 0,1/8)});

	trigger=Impulse.kr(0);
	env1=Env.adsr( attackTime:0,decayTime:0.2,sustainLevel:1,releaseTime:decaynoise);
	env2=Env.perc(0,decay2);
	freq=EnvGen.kr(env2,gate:trigger );
	amp=EnvGen.kr(env1,gate:trigger  , doneAction:2);

	a=RHPF.ar(Mix([o1,o2]),600,0.4);

	b=a*freq*vol*1;

	d=n*amp;
	c=Mix([RLPF.ar(d,14000,0.1,mul:vol/2),
	RHPF.ar(d,10000,0.3,mul:vol/2)]);

	Out.ar([2,3] ,Mix([c,b]))
}).add;
)
MIDIIn.connectAll;
MIDIFunc.noteOn({
		arg ...args;
		var note,params;
	Synth("bassdrum");
		},chan:5);

Synth("bassdrum")
Synth("snare")


ServerOptions.outDevices
ServerOptions.devices
Server.default.options.outDevice
MIDIFunc.trace

MIDIClient.sources[0].uid
MIDIIn.findPort("IAC Driver", "Bus 1").uid
MIDIClient.init;
MIDIClient.sources[0].uid
Scale.choose.degrees+1500
			TempoClock.default.tempo = 115*4/60;