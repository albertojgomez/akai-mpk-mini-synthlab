StormGUI {
	var <>guiSize;
	*new {
		^super.new.init;
    }

	init{
		guiSize = 0.7;
		^this;
	}



}

/*
window = Window.new("St0rmB0tn3t", 1000@500).front;
window.view.decorator = FlowLayout( window.view.bounds, 0@0, 0@0 );

*getGUI{
		|synthlab|

		^View(StormServer.singleton.window,(1000@240)*StormServer.guiSize);
	}

guiSize = 0.7;