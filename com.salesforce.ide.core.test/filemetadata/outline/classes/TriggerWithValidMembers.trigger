trigger MyTrigger on Account (before update) {
	static Integer staticVar;
	Integer instanceVar;

  class MyInnerClass {
    public void innerClassMethod() {
    }
  }
	
	for(Account a : Trigger.new) {
		system.debug('here');
		system.debug(a); // placeholder
	}
}
