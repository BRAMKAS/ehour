package net.rrm.ehour.ui.report.panel.detail

import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}
import net.rrm.ehour.ui.report.panel.DetailedReportDataObjectMother
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DetailedReportChartGeneratorTest extends FunSuite with Matchers with BeforeAndAfter {
  val reportData = DetailedReportDataObjectMother.getFlatReportData

  test("should have hourly based data set") {
    val seriesList = DetailedReportChartGenerator.generateHourBasedDetailedChartData(reportData)

    val series = seriesList.head
    series.name should equal("A Company")

    series.data.size should be(5)
    series.data.head.value should be(5.0)
  }
}