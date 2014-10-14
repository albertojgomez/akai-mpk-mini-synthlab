+ Event{
	forEach{
		|callback|
		this.keys.asArray.do({
			|key,index|
			callback.value(this[key],key);
		})
	}

	atIndex{
		|index|
		var keys;
		keys = this.keys.asArray();
		if (index >= size ){
		  ^nil;
		}{
			^this[keys[index]];
		}
	}
}
