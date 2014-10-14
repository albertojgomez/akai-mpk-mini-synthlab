StormObject{
	classvar width = 60 , height = 60;
	var <>view;

	*new {
		|parent, bgColor|
		^super.new.initStormObject(parent, bgColor);
    }

	initStormObject {
		| parent = nil, bgColor = nil |
		if (bgColor.isNil,{bgColor = Color.white});
		if (parent.isNil,{parent = StormServer.view.front });
		^this.makeGUI(parent);
	}

	makeGUI {
		|parent|
		{
			view = View.new(parent,
				this.getDimensions()).background_(Color.rand);
			view.decorator = FlowLayout( view.bounds, 0@0, 0@0 );
		}.defer;
		^this;
	}

	doesNotUnderstand { arg selector...args;
        Exception(">>>>>>>>>>>>StormError:" ++ this.class++" does not understand "++selector).throw;
    }

}