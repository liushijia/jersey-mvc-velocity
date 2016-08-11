package org.glassfish.jersey.server.mvc.velocity;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.TreeMap;

/**
 * Created by fengzh on 8/10/16.
 */
public class VelocityTemplateTest extends JerseyTest {

    @Path("/hello")
    public static class HelloResource {

        @GET
        @Path("/absolute")
        @Template(name = "/hello.vm")
        public TreeMap<String, String> getByLocation() {
            return new TreeMap<String, String>() {{
                put("name", "absolute");
            }};
        }

        @GET
        @Path("/withoutSuffix")
        @Template(name = "/hello")
        public TreeMap<String, String> getHelloByName() {
            return new TreeMap<String, String>() {{
                put("name", "withoutSuffix");
            }};
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new ResourceConfig(HelloResource.class, VelocityMvcFeature.class)
                .property(VelocityMvcFeature.TEMPLATE_BASE_PATH, "templates")
                .property(VelocityMvcFeature.ENCODING, "UTF-8");

    }

    @Test
    public void testAbsolute() throws IOException {
        String output = target("/hello/absolute").request().get(String.class);
        System.out.println(output);
        Assert.assertThat(output, CoreMatchers.equalTo("Hello absolute!"));
    }

    @Test
    public void testWithoutSuffix() throws IOException {
        String output = target("/hello/withoutSuffix").request().get(String.class);
        System.out.println(output);
        Assert.assertThat(output, CoreMatchers.equalTo("Hello withoutSuffix!"));
    }
}
