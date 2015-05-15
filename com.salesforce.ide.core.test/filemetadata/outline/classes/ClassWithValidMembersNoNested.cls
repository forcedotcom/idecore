public class ClassWithValidMembersNoNested {
    // FieldClassMember (static)
    private static Integer staticField;
    
    // FieldClassMember
    private Integer field;
    
    // FieldClassMember (all in one line)
    private Integer field1, field2, field3;
    
    // PropertyClassMember
    private Integer property { get; set}
    
    // StaticInitializerClassMember
    static {
        staticField = 1;
    }
    
    // InitializerClassMember
    {
        field = 1;
    }
    
    // MethodClassMember
    public ClassWithValidMembersNoNested() {
        
    }
    
}