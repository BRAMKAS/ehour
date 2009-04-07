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

package net.rrm.ehour.ui.report.panel.aggregate;

import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.ui.common.report.ReportConfig;
import net.rrm.ehour.ui.report.ReportDrawType;
import net.rrm.ehour.ui.report.TreeReport;
import net.rrm.ehour.ui.report.TreeReportData;
import net.rrm.ehour.ui.report.chart.AggregateChartDataConverter;
import net.rrm.ehour.ui.report.chart.aggregate.AggregateChartImage;
import net.rrm.ehour.ui.report.chart.aggregate.CustomerHoursAggregateChartDataConverter;
import net.rrm.ehour.ui.report.chart.aggregate.CustomerTurnoverAggregateChartDataConverter;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;

/**
 * Customer report panel
 **/

public class CustomerReportPanel extends AggregateReportPanel
{
	private static final long serialVersionUID = 8422287988040603274L;

	public CustomerReportPanel(String id, TreeReport report)
	{
		this(id, report, ReportDrawType.IMAGE);
	}

	public CustomerReportPanel(String id, TreeReport report, ReportDrawType drawType)
	{
		super(id, report, ReportConfig.AGGREGATE_CUSTOMER, CustomerReportExcel.getId(), drawType);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.ui.report.panel.type.ReportPanel#addCharts(net.rrm.ehour.report.reports.ReportDataAggregate, org.apache.wicket.markup.html.WebMarkupContainer)
	 */
	@Override
	protected void addCharts(String hourId, String turnOverId, ReportData data, WebMarkupContainer parent)
	{
		ReportData rawData = ((TreeReportData)data).getRawReportData();
		Model dataModel = new Model(rawData);

		AggregateChartDataConverter hourConverter = new CustomerHoursAggregateChartDataConverter();
		Image customerHoursChart = new AggregateChartImage(hourId, dataModel, getChartWidth(), getChartHeight(), hourConverter);
		parent.add(customerHoursChart);

		AggregateChartDataConverter turnoverConverter = new CustomerTurnoverAggregateChartDataConverter();
		Image customerTurnoverChart = new AggregateChartImage(turnOverId, dataModel, getChartWidth(), getChartHeight(), turnoverConverter);
		parent.add(customerTurnoverChart);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.ui.report.panel.aggregate.AggregateReportPanel#addFlashCharts(net.rrm.ehour.report.reports.ReportData, org.apache.wicket.markup.html.WebMarkupContainer)
	 */
	@Override
	protected void addFlashCharts(String hourId, String turnOverId, ReportData data, WebMarkupContainer parent)
	{
		ReportData rawData = ((TreeReportData)data).getRawReportData();
		
		parent.add(createHorizontalFlashChart(hourId, rawData, new CustomerHoursAggregateChartDataConverter()));
		parent.add(createHorizontalFlashChart(turnOverId, rawData, new CustomerTurnoverAggregateChartDataConverter()));
		
	}
}
