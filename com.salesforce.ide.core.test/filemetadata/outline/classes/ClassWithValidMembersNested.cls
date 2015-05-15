public class ClassWithValidMembersNoNested {
    // InnerEnumMember
    enum InnerEnum {
        YES, NO
    }
    
    // InnerClassMember
    class InnerClass {
        Integer field;
    }
    
    // InnerInterfaceMember
    interface InnerInterface {
        void innerInterfaceMethod();
    }
}