+ Event{
	forEach{
		|callback|
		this.keys.asArray.do({
			|key,index|
			callback.value(this[key],key);
		})
	}
}