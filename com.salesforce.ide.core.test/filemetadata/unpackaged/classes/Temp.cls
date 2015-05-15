public class Temp {
  public String echo(String str) {
     return 'Force.com IDE ' + str ;
  }

  public String echo1(String str) {
     return str;
  }

  public String echo2(String str) {
     return str;
  }

  static testMethod void testEcho() {
     Temp  c = new Temp();
     System.assertEquals('Force.com IDE wall', c.echo('wall'));
  }

}