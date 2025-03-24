package ut.GleanSearchConfigRestPlugin;

import org.junit.Test;
import ScioSearchConfigRestPlugin.api.MyPluginComponent;
import ScioSearchConfigRestPlugin.impl.MyPluginComponentImpl;

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