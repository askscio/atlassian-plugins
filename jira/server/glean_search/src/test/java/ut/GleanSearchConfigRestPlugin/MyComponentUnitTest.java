package ut.GleanSearchConfigRestPlugin;

import org.junit.Test;
import GleanSearchConfigRestPlugin.api.MyPluginComponent;
import GleanSearchConfigRestPlugin.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}