/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2008-2010 by chenillekit.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 *
 */

package org.chenillekit.access.services.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.TransformMethod;

import org.chenillekit.access.annotations.ManagedRestricted;
import org.chenillekit.access.dao.ProtectionRule;
import org.chenillekit.access.dao.ProtectionRuleDAO;
import org.slf4j.Logger;

/**
 * @version $Id$
 */
public class ManagedRestrictedWorker extends RestrictedWorker
{
	private final Logger logger;
	private final ComponentClassResolver resolver;
	private final ProtectionRuleDAO protectionRuleDAO;

	public ManagedRestrictedWorker(Logger logger,
								   ComponentClassResolver resolver,
								   ProtectionRuleDAO protectionRuleDAO)
	{
		super(logger);
		this.logger = logger;
		this.resolver = resolver;
		this.protectionRuleDAO = protectionRuleDAO;
	}

	/**
	 * Read and process restriction on page classes annotated with {@link org.chenillekit.access.annotations.ManagedRestricted} annotation
	 *
	 * @param transformation Contains class-specific information used when transforming a raw component class
	 *                       into an executable component class.
	 * @param model		  Mutable version of {@link org.apache.tapestry5.model.ComponentModel} used during
	 *                       the transformation phase.
	 */
	@Override
	protected void processPageRestriction(ClassTransformation transformation, MutableComponentModel model)
	{
		ManagedRestricted restricted = transformation.getAnnotation(ManagedRestricted.class);
		if (restricted == null)
			return;

		String pagename = resolver.resolvePageClassNameToPageName(model.getComponentClassName());

		if (logger.isDebugEnabled())
			logger.debug("searching permission for component {}", pagename);

		ProtectionRule protectionRule = protectionRuleDAO.retrieveProtectionRule(pagename);

		/**
		 * there is no protection rule for that component
		 */
		if (protectionRule == null)
			return;

		if (logger.isDebugEnabled())
			logger.debug("found permission groups {} for component {}", protectionRule.getGroups(), pagename);


		setGroupRoleMeta(true, model, null, null, protectionRule.getGroups(), protectionRule.getRoleWeight());
	}

	/**
	 * inject meta datas about annotated methods
	 *
	 * @param transformation Contains class-specific information used when transforming a raw component class
	 *                       into an executable component class.
	 * @param model		  Mutable version of {@link org.apache.tapestry5.model.ComponentModel} used during
	 *                       the transformation phase.
	 */
	@Override
	protected void processEventHandlerRestrictions(ClassTransformation transformation, MutableComponentModel model)
	{
		List<TransformMethod> matchedMethods = getMatchedMethods(transformation, ManagedRestricted.class);

		for (TransformMethod method : matchedMethods)
		{
			String pagename = resolver.resolvePageClassNameToPageName(model.getComponentClassName());
			String componentId = extractComponentId(method, method.getAnnotation(OnEvent.class));
			String eventType = extractEventType(method, method.getAnnotation(OnEvent.class));


			ProtectionRule protectionRule = protectionRuleDAO.retrieveProtectionRule(String.format("%s.%s", pagename, eventType));

			/**
			 * there is no protection rule for that component
			 */
			if (protectionRule == null)
				continue;

			if (logger.isDebugEnabled())
				logger.debug("found permission groups {} for event {}",
							 Arrays.toString(protectionRule.getGroups()), String.format("%s.%s", pagename, eventType));

			setGroupRoleMeta(false, model, componentId, eventType, protectionRule.getGroups(), protectionRule.getRoleWeight());
		}
	}
}