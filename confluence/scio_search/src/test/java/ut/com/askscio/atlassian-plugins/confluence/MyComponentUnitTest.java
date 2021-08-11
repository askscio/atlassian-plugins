package ut.com.askscio.atlassian-plugins.confluence;

import org.junit.Test;
import com.askscio.atlassian-plugins.confluence.api.MyPluginComponent;
import com.askscio.atlassian-plugins.confluence.impl.MyPluginComponentImpl;

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