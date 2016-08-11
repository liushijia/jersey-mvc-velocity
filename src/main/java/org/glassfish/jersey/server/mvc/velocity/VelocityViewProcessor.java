/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server.mvc.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.tools.config.Configuration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

final class VelocityViewProcessor extends AbstractTemplateProcessor<Template> {

    private final VelocityConfigurationFactory factory;

    @Inject
    public VelocityViewProcessor(final javax.ws.rs.core.Configuration config, final ServiceLocator serviceLocator,
                                 @Optional final ServletContext servletContext) {
        super(config, servletContext, "velocity", "vm");

        this.factory = getTemplateObjectFactory(serviceLocator, VelocityConfigurationFactory.class,
                new Value<VelocityConfigurationFactory>() {
                    @Override
                    public VelocityConfigurationFactory get() {
                        Configuration configuration = getTemplateObjectFactory(serviceLocator, Configuration.class,
                                Values.<Configuration>empty());
                        if (configuration == null) {
                            return new VelocityDefaultConfigurationFactory(servletContext);
                        } else {
                            return new VelocitySuppliedConfigurationFactory(configuration);
                        }
                    }
                });
        Velocity.init();
    }

    @Override
    protected Template resolve(final String templateReference, final Reader reader) throws Exception {
        return Velocity.getTemplate(templateReference, "UTF-8");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeTo(final Template template, final Viewable viewable, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream out) throws IOException {
        try {
            Object model = viewable.getModel();
            if (!(model instanceof Map)) {
                model = new HashMap<String, Object>() {{
                    put("model", viewable.getModel());
                }};
            }

            VelocityContext velocityContext = new VelocityContext();
            for (String key : ((Map<String, Object>) model).keySet()) {
                velocityContext.put(key, ((Map<String, Object>) model).get(key));
            }

            Charset encoding = setContentType(mediaType, httpHeaders);

            VelocityWriter writer = new VelocityWriter(new OutputStreamWriter(out, encoding));

            template.merge(velocityContext, writer);
            writer.flush();

        } catch (VelocityException te) {
            throw new ContainerException(te);
        }
    }
}
