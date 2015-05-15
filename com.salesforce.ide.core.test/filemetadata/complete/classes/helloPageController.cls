public class helloPageController {

    public static testmethod void t1() {
        goodMethod();
        getFax();
    }

    public static String getFax() {
        return 'GoodFax';
    }

    public static void goodMethod() {
        System.debug('I am good');
    }
}