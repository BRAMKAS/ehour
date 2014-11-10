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

package net.rrm.ehour.ui.report.page;

import com.google.common.collect.Sets;
import net.rrm.ehour.domain.*;
import net.rrm.ehour.report.criteria.AvailableCriteria;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.report.criteria.ReportCriteriaUpdateType;
import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.report.reports.element.AssignmentAggregateReportElement;
import net.rrm.ehour.report.service.AggregateReportService;
import net.rrm.ehour.report.service.DetailedReportService;
import net.rrm.ehour.report.service.ReportCriteriaService;
import net.rrm.ehour.ui.common.BaseSpringWebAppTester;
import net.rrm.ehour.ui.common.event.AjaxEvent;
import net.rrm.ehour.ui.common.model.KeyResourceModel;
import net.rrm.ehour.ui.report.panel.criteria.ReportCriteriaAjaxEventType;
import net.rrm.ehour.ui.report.panel.criteria.ReportCriteriaBackingBean;
import net.rrm.ehour.ui.report.panel.criteria.ReportTabbedPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("serial")
public class ReportPageTest extends BaseSpringWebAppTester implements Serializable {
    private MockReportTabBuilder mockReportTabCommand;
    protected ReportCriteriaService reportCriteriaService;
    protected AggregateReportService aggregateReportService;
    protected DetailedReportService detailedReportService;
    protected ReportData data;
    protected ReportCriteria reportCriteria;

    @Before
    public void setup() {
        mockReportTabCommand = new MockReportTabBuilder();

        getMockContext().putBean("reportTabBuilder", mockReportTabCommand);
    }

    @Test
    public void shouldRender() {
        when(reportCriteriaService.syncUserReportCriteria(any(ReportCriteria.class), eq(ReportCriteriaUpdateType.UPDATE_ALL)))
                .thenReturn(reportCriteria);

        startPage();

        tester.assertNoErrorMessage();
    }

    @Test
    public void shouldUpdateTabs() {
        mockReportTabCommand.returnTabs = createTabs(3);

        updateTabs();

        assertNotNull(mockReportTabCommand.argBean);
    }

    private void updateTabs() {
        when(reportCriteriaService.syncUserReportCriteria(any(ReportCriteria.class),
                eq(ReportCriteriaUpdateType.UPDATE_ALL)))
                .thenReturn(reportCriteria);

        startPage();

        ReportPage page = (ReportPage) tester.getLastRenderedPage();

        AjaxRequestTarget target = mock(AjaxRequestTarget.class);
        AjaxEvent event = new AjaxEvent(ReportCriteriaAjaxEventType.CRITERIA_UPDATED, target);

        page.ajaxEventReceived(event);

        verify(target).add(any(ReportTabbedPanel.class));
    }

    private List<ITab> createTabs(int amount) {
        List<ITab> tabs = new ArrayList<ITab>();

        for (int i = 0; i < amount; i++) {
            AbstractTab tab = new AbstractTab(new KeyResourceModel(Integer.toString(i))) {

                @Override
                public Panel getPanel(String panelId) {
                    return new EmptyPanel(panelId);
                }
            };

            tabs.add(tab);
        }

        return tabs;

    }

    private void startPage() {
        tester.startPage(ReportPage.class);

        tester.assertRenderedPage(ReportPage.class);
        tester.assertNoErrorMessage();
    }

    @Before
    public final void before() throws Exception {
        reportCriteriaService = mock(ReportCriteriaService.class);
        getMockContext().putBean("reportCriteriaService", reportCriteriaService);

        aggregateReportService = mock(AggregateReportService.class);
        getMockContext().putBean("aggregatedReportService", aggregateReportService);

        detailedReportService = mock(DetailedReportService.class);
        getMockContext().putBean("detailedReportService", detailedReportService);

        AvailableCriteria availCriteria = new AvailableCriteria();

        SortedSet<Customer> customers = Sets.newTreeSet();
        customers.add(new Customer(1));
        availCriteria.setCustomers(customers);

        Set<Project> projects = Sets.newHashSet();
        projects.add(new Project(2));
        availCriteria.setProjects(projects);

        List<UserDepartment> depts = new ArrayList<UserDepartment>();
        depts.add(new UserDepartment(2));
        availCriteria.setUserDepartments(depts);

        Set<User> usrs = Sets.newHashSet();
        usrs.add(new User(2));
        availCriteria.setUsers(usrs);

        reportCriteria = new ReportCriteria(availCriteria);

        List<AssignmentAggregateReportElement> agg = new ArrayList<AssignmentAggregateReportElement>();
        AssignmentAggregateReportElement pag = new AssignmentAggregateReportElement();
        ProjectAssignment ass = new ProjectAssignment(1);
        User user = new User(1);
        ass.setUser(user);

        Customer cust = new Customer(1);
        Project prj = new Project(1);
        prj.setCustomer(cust);
        ass.setProject(prj);
        pag.setProjectAssignment(ass);

        agg.add(pag);

        data = new ReportData(agg, reportCriteria.getReportRange(), reportCriteria.getUserSelectedCriteria());
    }

    private class MockReportTabBuilder implements ReportTabBuilder {
        ReportCriteriaBackingBean argBean;
        List<ITab> returnTabs;

        public List<ITab> createReportTabs(ReportCriteriaBackingBean backingBean) {
            this.argBean = backingBean;
            return returnTabs;
        }

    }
}
