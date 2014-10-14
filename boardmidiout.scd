(
o = MIDIOut.newByName("ProdipeMIDIlilo ", "ProdipeMIDIlilo ");

x = Pdefn(\bd,Pseq([36,\rest,\rest,\rest], inf)).quant_(64);
y = Pdefn(\sd,Pseq([\rest,\rest,\rest,\rest,38,\rest,\rest,\rest], inf)).quant_(64);
z = Pdefn(\hh,Pseq([\rest,\rest,46,\rest], inf)).quant_(64);


a = Pbind(\type, \midi,\midiout , o,\midinote , Pdefn(\bd));
b = Pbind( \type, \midi, \midiout , o, \midinote , Pdefn(\sd));
c = Pbind( \type, \midi, \midiout , o, \midinote , Pdefn(\hh));

)
p = Ppar([a, b, c],inf).play;

Pdefn(\bd,Pseq([36,\rest,\rest,\rest,36,36,\rest,36], inf)).quant_(64);
Pdefn(\sd,Pseq([\rest,\rest,46,\rest], inf)).quant = 64;


p.stop





Pbindef(\bdf, \midinote, Pseq([36,\rest,\rest,\rest], inf))
Pbindef(\sdf, \midinote, Pseq([38,\rest,\rest,\rest,37,\rest,\rest,\rest], inf))

Pdef(\re,Pbind()).class
Pbindf

Ndef(\tr)

s = y.asStream
s.next

(
Pdefn(\x,Pseq([1,\rest,\rest,\rest], inf).trace);
Pdefn(\x).quant = 64;
Pbind(\degree,Pdefn(\x)).play;
)
Pdefn(\x,Pseq([2,\rest,\rest,\rest], inf).trace);