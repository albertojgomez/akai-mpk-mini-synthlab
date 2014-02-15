
(

l=SynthLab(\PWM,{
	|freq=100,subosc_sawvol = 0.5,subosc_sawtune = 0.01,pulse_range = 0.2,rev_size=0.1,
	rev_mix = 0.33,rev_damp=0.5|
	var pwm,saw;
	pwm = Pulse.ar(freq,SinOsc.kr(1).range(0.5 - pulse_range,0.5 + pulse_range),0.8);
	saw = LFSaw.ar((freq + (freq * (subosc_sawtune - 0.5) / 50) ) / 2 ,mul:subosc_sawvol);
	FreeVerb2.ar(Mix([pwm,saw]),Mix([pwm,saw]),rev_mix,rev_size*10,rev_damp);
});

)
StormPattern(l)
a = l.getParamsArray[17]
a.
a.add(100)
b = a.asArray
b[\freq] = 100
a
b.asList

(

f=SynthLab(\sigma,{
	|freq=300|
	a = PMOsc.ar(freq, VarSaw.kr(freq,0.5).range(freq*0.5,freq*1.5), Pulse.ar(freq*1.01), 0, 1).distort;
	GVerb.ar(a,1,1,0.01,0.0,6);

} );

)


(

l=SynthLab(\FM,{
|mod_ratio = 1.378,   // 689/500
        freq = 500,
        mod_index = 200|

SinOsc.ar(freq + (SinOsc.ar(freq * mod_ratio) * mod_index));
},2);

)

(

l=SynthLab(\grainPWM,{
//	HenonN
	|freq=100,duration=0.5,gausswidth=0.1|
	Pulse.ar(freq,LFGauss.kr(duration,gausswidth).range(0.1,0.9),0.8) ;
},3 );

)


(

l=SynthLab(\FMGrain,{
	|grain_density=10,grain_vol=0.4,grain_duration=0.01,freq,freq2,delay_time=0.2,delay_max=0.2,
	reve_size=5,reve_time=3,reve_damp=0.5,reve_spread=15,reve_dry=1
	|
	var s;
	s = GrainFM.ar(2,Dust.ar(grain_density*100,grain_vol),grain_duration,freq,freq2);
	GVerb.ar(s,reve_size,reve_time,reve_damp, spread:reve_spread,drylevel:reve_dry);
},1);

)


(
o=s.options;
o.memSize;
o.memSize = 2.pow(18);
s.quit;
s.boot;
)
w=Synth(\PWM,[\freq,150]);
w.set(\gate,0)
w.get(\gate)
w.node
l.play



