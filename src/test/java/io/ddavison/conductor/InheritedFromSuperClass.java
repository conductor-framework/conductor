package io.ddavison.conductor;

import org.testng.annotations.Test;

public class InheritedFromSuperClass extends SuperClassTest {

    @Test
    public void testInheritsConfigProperly() {
        setText("[value='some text']", "test");
    }
}
