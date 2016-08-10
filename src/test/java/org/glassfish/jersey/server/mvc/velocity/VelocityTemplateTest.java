package org.glassfish.jersey.server.mvc.velocity;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.TreeMap;

/**
 * Created by fengzh on 8/10/16.
 */
public class VelocityTemplateTest extends JerseyTest {

    @Path("/hello")
    public static class HelloResource {


        @GET
        @Path("/{name}")
        @Produces(MediaType.TEXT_HTML)
        public String getHello(@PathParam("name") String name) {
            return "hello " + name;
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HelloResource.class, VelocityMvcFeature.class);
    }

    @Test
    public void test() throws IOException {
        String output = target("/hello/velocity").request().get(String.class);
        System.out.println(output);
    }
}
