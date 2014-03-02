StormServer.s
(

l=StormSynth(\PWM,{
	|freq=100,subosc_sawvol = 0.5,subosc_sawtune = 0.01,pulse_range = 0.2,rev_size=0.1,
	rev_mix = 0.33,rev_damp=0.5,q,w,e,r,t,y,u,i,o,p,a,s,d,f,g,h,j,k,l,z,x,c,v,b,m,qa,ws,ed,rf,tg,yh,uj,ik,ol,az,sx,dc,fv,gb,hn,jm,kl,wer,rty,gh,sdf,dhgf,gkjh,dgf,ads,asdf,aq,sw,de,fr,gt,hy,ju,ki,lo,br,nt|
	var pwm,saw;
	pwm = Pulse.ar(freq,LFTri.kr(1).range(0.5 - pulse_range,0.5 + pulse_range),0.8);
	saw = LFSaw.ar((freq + (freq * (subosc_sawtune - 0.5) / 50) ) / 2 ,mul:subosc_sawvol);
	FreeVerb2.ar(Mix([pwm,saw]),Mix([pwm,saw]),rev_mix,rev_size*10,rev_damp);
});

)

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

l.controlEvent.keys.asArray
l.getParamsArray
w=Synth(\PWM,[\freq,150]);


w.set(\gate,0)
w.get(\gate)
w.node

a =View.new(Window.new('LEL',1000@800),1000@200).front
a.decorat
MIDIFunc.trace

