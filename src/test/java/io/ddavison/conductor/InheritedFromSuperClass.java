package io.ddavison.conductor;

import org.junit.Test;

public class InheritedFromSuperClass extends SuperClassTest {

    @Test
    public void testInheritsConfigProperly() {
        setText("[value='some text']", "test");
    }
}
