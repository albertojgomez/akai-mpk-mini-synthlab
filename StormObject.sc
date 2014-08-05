StormObject{
	classvar width = 60 , height = 60;
	var <>view;

	*new {
		^super.new.initStormObject;
    }

	initStormObject {
		| parent = nil, bgColor = nil |
		if (bgColor.isNil,{bgColor = Color.white});
		if (parent.isNil,{parent = StormServer.view.front });
		view = View.new(parent,
			Point(width,height)).background_(Color.white);
		^this;
	}

	doesNotUnderstand { arg selector...args;
        (">>>>>>>>>>>>StormError:" ++ this.class++" does not understand method "++selector);
    }

}