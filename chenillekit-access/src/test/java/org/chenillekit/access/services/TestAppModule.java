/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2008 by chenillekit.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.chenillekit.access.services;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateCreator;

import org.chenillekit.access.utils.WebUser;
import org.chenillekit.access.utils.RootUser;

/**
 * @author <a href="mailto:shomburg@hsofttec.com">S.Homburg</a>
 * @version $Id$
 */
public class TestAppModule
{
    public static void contributeASOs(MappedConfiguration<Class, ApplicationStateContribution> configuration)
    {
        ApplicationStateCreator<RootUser> creator = new ApplicationStateCreator<RootUser>()
        {
            public RootUser create()
            {
                return new RootUser();
            }
        };

        configuration.add(RootUser.class, new ApplicationStateContribution("session", creator));
    }

    public static void contributeAccessControllerDispatcher(MappedConfiguration<String, Class> configuration)
    {
        configuration.add("webuser.implementation", RootUser.class);
    }
}