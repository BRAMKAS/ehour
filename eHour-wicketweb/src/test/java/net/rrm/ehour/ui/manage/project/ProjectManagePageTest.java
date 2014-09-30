/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.ui.manage.project;

import com.richemont.jira.JiraService;
import com.richemont.windchill.WindChillUpdateService;
import net.rrm.ehour.customer.service.CustomerService;
import net.rrm.ehour.domain.Customer;
import net.rrm.ehour.domain.ProjectObjectMother;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.project.service.ProjectService;
import net.rrm.ehour.ui.common.BaseSpringWebAppTester;
import net.rrm.ehour.user.service.UserService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.*;

public class ProjectManagePageTest extends BaseSpringWebAppTester {
    private ProjectService projectService;
//    private UserService userService;
    private CustomerService customerService;

    @Before
    public void before() throws Exception {
        WindChillUpdateService windChillUpdateService = createMock(WindChillUpdateService.class);
        getMockContext().putBean(windChillUpdateService);

        JiraService jiraService = createMock(JiraService.class);
        getMockContext().putBean(jiraService);

        projectService = createMock(ProjectService.class);
        getMockContext().putBean("projectService", projectService);

        customerService = createMock(CustomerService.class);
        getMockContext().putBean("customerService", customerService);
    }

    @Test
    public void shouldRender() {
        expect(customerService.getActiveCustomers())
                .andReturn(new ArrayList<Customer>());

        expect(projectService.getActiveProjects())
                .andReturn(Arrays.asList(ProjectObjectMother.createProject(1)));

        replay(projectService, /*userService, */customerService);

        tester.startPage(ProjectManagePage.class);
        tester.assertRenderedPage(ProjectManagePage.class);
        tester.assertNoErrorMessage();

        verify(projectService, /*userService, */customerService);
    }
}
