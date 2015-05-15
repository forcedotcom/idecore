public class Test {
	
	static testMethod void testSuccess(){
		 Temp  c = new Temp();
		 System.assertEquals('Force.com IDE matt', c.echo('matt'));
	}
	
	
	static testMethod void testFail(){
		 Temp  c = new Temp();
		 System.assertEquals('matt', c.echo('matt'));
	}
}