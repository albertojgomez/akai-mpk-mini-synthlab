StormServer.loadSession("/Users/Alberto/Documents/REAPER Media/lexicon/archive.sctxar")
StormServer.saveSession("/Users/Alberto/Documents/REAPER Media/lexicon/archive.sctxar")
StormServer.panic




StormServer.s.instruments[1].name
StormServer.instrumentExists(\PWM)
StormServer.getStormGUI.instrumentGUIs[\PWM][\knobs].keys.do({|i|
	if (prevControlEvent[i].isNil.not){
		StormServer.getStormGUI.instrumentGUIs[\PWM][\knobs][i].valueAction_(prevControlEvent[i].map)
	}
});
(

l=StormSynth(\PWM,{
	|freq=100,subosc_sawvol = 0.5,subosc_sawtune = 0.01,pulse_range = 0.2,rev_size=0.1,
	rev_mix = 0.33,rev_damp=0.5,lel = 0.5|
	var pwm,saw;
	pwm = Pulse.ar(freq,LFTri.kr(1).range(0.5 - pulse_range,0.5 + pulse_range),0.8);
	saw = LFSaw.ar((freq + (freq * (subosc_sawtune * SinOsc.kr(1/5).range(0.7,1.3) - 0.5) / 50) ) / 2 ,mul:subosc_sawvol);
	FreeVerb2.ar(Mix([pwm,saw]),Mix([pwm,saw]),rev_mix,rev_size*10,rev_damp);
});

)


(
l=StormSynth(\PWMe,{
	|freq=100,rev_size=0.1,	rev_mix = 0.33,rev_damp=0.5,fm_ratio = 0.5,fm_freq = 100|
	var car,mod,s1,s2;
	mod = SinOsc.kr(freq) * fm_ratio;
	car = VarSaw.ar(freq * mod, 0 , SinOsc.kr(freq).range(0.6,0.6));
	s1 = BPF.ar(car,SinOsc.kr(7).exprange(432,864));
	s2 = BPF.ar(car,SinOsc.kr(7,0.5).exprange(432,864));

	FreeVerb2.ar(s1,s2,rev_mix,rev_size*10,rev_damp);
});
)

/*******SYNTHS******/
(

f=StormSynth(\sigma,{
	|freq=300|
	a = PMOsc.ar(freq, VarSaw.kr(freq,0.5).range(freq*0.5,freq*1.5), Pulse.ar(freq*1.01), 0, 1).distort;
	GVerb.ar(a,1,1,0.01,0.0,6);

});
)

(
l=StormSynth(\grainPWM,{
//	HenonN
	|freq=100,duration=0.5,gausswidth=0.1|
	Pulse.ar(freq,LFGauss.kr(duration,gausswidth).range(0.1,0.9),0.8) ;
},3 );
)