/**
 * Created on May 8, 2007
 * Created by Thies Edeling
 * Copyright (C) 2005, 2006 te-con, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * thies@te-con.nl
 * TE-CON
 * Legmeerstraat 4-2h, 1058ND, AMSTERDAM, The Netherlands
 *
 */

package net.rrm.ehour.ui;

import net.rrm.ehour.ui.page.admin.assignment.AssignmentPage;
import net.rrm.ehour.ui.page.login.LoginPage;
import net.rrm.ehour.ui.page.user.OverviewPage;
import net.rrm.ehour.ui.page.user.timesheet.Page2;
import net.rrm.ehour.ui.session.EhourWebSession;
import net.rrm.ehour.util.EhourConstants;

import org.acegisecurity.AuthenticationManager;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.PackageName;

/**
 * Base config for wicket eHour webapp
 **/

public class EhourWebApplication extends AuthenticatedWebApplication
{
	private AuthenticationManager authenticationManager;

	public void init()
	{
		super.init();
		
		System.out.println("atuh: " + authenticationManager);

		mount("/admin", PackageName.forClass(AssignmentPage.class));
		mount("/consultant", PackageName.forPackage(OverviewPage.class.getPackage()));
		mount("/consultant/timesheet", PackageName.forPackage(Page2.class.getPackage()));
		getRequestCycleSettings().setResponseRequestEncoding("UTF-8");

		addComponentInstantiationListener(new SpringComponentInjector(this));

		getSecuritySettings().setAuthorizationStrategy(new MetaDataRoleAuthorizationStrategy(this));

        MetaDataRoleAuthorizationStrategy.authorize(OverviewPage.class, EhourConstants.ROLE_CONSULTANT);
        MetaDataRoleAuthorizationStrategy.authorize(AssignmentPage.class, EhourConstants.ROLE_ADMIN);		
	}

	/**
	 * Set the homepage
	 */
	@Override
	public Class getHomePage()
	{
		return OverviewPage.class;
	}

	protected Class getSignInPageClass()
	{
		return LoginPage.class;
	}

	/*
	 * 
	 */
	public AuthenticationManager getAuthenticationManager()
	{
		return authenticationManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.wicket.authentication.AuthenticatedWebApplication#getWebSessionClass()
	 */
	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass()
	{
		return EhourWebSession.class;
	}

	/**
	 * @param authenticationManager the authenticationManager to set
	 */
	public void setAuthenticationManager(AuthenticationManager authenticationManager)
	{
		this.authenticationManager = authenticationManager;
	}
}
