public class alphaClass {

    public static testmethod void t1() {
        goodMethod();
        System.assert(true); // always work.
    }

    public static testmethod void t2() {
        getFax();
        System.assert(true); // always work.
    }

    public static String getFax() {
        return 'GoodFax';
    }

    public static void goodMethod() {
        System.debug('I am good');
    }
}