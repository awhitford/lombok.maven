package org.projectlombok.test;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class DataExampleTest {

    @Test
    public void testDataExample() {
        val name = "MyData";
        val de = new DataExample(name);
        Assert.assertEquals(de.getName(), name);
    }

}
