package hyung.jin.seo.jae.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import hyung.jin.seo.jae.JaeApplication;
import hyung.jin.seo.jae.dto.AssessmentAnswerDTO;
import hyung.jin.seo.jae.dto.BranchDTO;
import hyung.jin.seo.jae.dto.GuestStudentAssessmentDTO;
import hyung.jin.seo.jae.dto.StudentDTO;
import hyung.jin.seo.jae.dto.TestResultHistoryDTO;
import hyung.jin.seo.jae.model.GuestStudent;
import hyung.jin.seo.jae.model.Student;
import hyung.jin.seo.jae.model.TestAnswerItem;
import hyung.jin.seo.jae.service.PdfService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;

@Service
public class PdfServiceImpl implements PdfService {

	@Autowired
	private ResourceLoader resourceLoader;

	@Override
	public byte[] generateEmptyTestResult(Long studentId) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter pdfWriter = new PdfWriter(baos);
			PdfDocument pdfDocument = new PdfDocument(pdfWriter);
			pdfDocument.setDefaultPageSize(PageSize.A4);
			Document document = new Document(pdfDocument);
			document.add(new Paragraph("No test result found for student with ID: " + studentId)
					.setFontSize(12f)
					.setBold()
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginTop(200));
			document.close();
			byte[] pdfData = baos.toByteArray();
			return pdfData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] generateTestResult(Map<String, Object> data) {

		// prepare ingredients
		StudentDTO student = (StudentDTO) data.get(JaeConstants.STUDENT_INFO);
		String testGroupName = (String) data.get(JaeConstants.TEST_GROUP_INFO);
		BranchDTO branch = (BranchDTO) data.get(JaeConstants.BRANCH_INFO);
		String volume = (String) data.get(JaeConstants.VOLUME_INFO);
		List<String> testTitles = (List<String>) data.get(JaeConstants.TEST_TITLE_INFO);
		List<Integer> testAnswerTotals = (List<Integer>) data.get(JaeConstants.TEST_ANSWER_TOTAL_COUNT);
		List<Integer> studentAnswerCorrects = (List<Integer>) data.get(JaeConstants.STUDENT_ANSWER_CORRECT_COUNT);
		List<Double> scores = (List<Double>) data.get(JaeConstants.STUDENT_SCORE);
		List<Double> averages = (List<Double>) data.get(JaeConstants.TEST_AVERAGE_SCORE);
		List<Double> highests = (List<Double>) data.get(JaeConstants.TEST_HIGHEST_SCORE);
		List<Double> lowests = (List<Double>) data.get(JaeConstants.TEST_LOWEST_SCORE);


		List<List<Integer>> studentAnswers = (List<List<Integer>>) data.get(JaeConstants.STUDENT_ANSWERS);
		List<List<TestAnswerItem>> testAnswers = (List<List<TestAnswerItem>>) data.get(JaeConstants.TEST_ANSWERS);
		List<List<TestResultHistoryDTO>> histories = (List<List<TestResultHistoryDTO>>) data.get(JaeConstants.TEST_RESULT_HISTORY);

		// List<GuestStudentAssessmentDTO> gsas = (List<GuestStudentAssessmentDTO>) data.get(JaeConstants.STUDENT_ANSWER);
		// List<AssessmentAnswerDTO> aas = (List<AssessmentAnswerDTO>) data.get(JaeConstants.CORRECT_ANSWER);
		

		try {
			// // Set the content type and attachment header.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter pdfWriter = new PdfWriter(baos);
			PdfDocument pdfDocument = new PdfDocument(pdfWriter);
			pdfDocument.setDefaultPageSize(PageSize.A4);
			Document document = new Document(pdfDocument);
			Paragraph onespace = new Paragraph("\n");
			float wholeWidth = pdfDocument.getDefaultPageSize().getWidth(); // whole width

			// 1. student section
			Table header = getStudentTable(wholeWidth, testGroupName, testTitles.get(0) + " " + volume, student);
			document.add(header);
			document.add(onespace);

			// 2. title section
			Table totalScore = new Table(new float[]{wholeWidth});
			totalScore.addCell(detailCell("You have scored " + studentAnswerCorrects.get(0)+ " out of " + testAnswerTotals.get(0)+ " (" + scores.get(0) + "%)").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			document.add(totalScore);

			// 3. answer section
			Table detailScore = getDetailAnswer(wholeWidth, studentAnswers.get(0), testAnswers.get(0));
			document.add(detailScore);
			document.add(onespace);

			// 4. statistics title section
			Table statsTitle = new Table(new float[]{wholeWidth});
			statsTitle.addCell(detailCell(testGroupName + " - " + JaeUtils.getGradeYearName(student.getGrade())+ " " + testTitles.get(0) + " " + volume + " Result").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			document.add(statsTitle);

			// 5. statistics section
			Table statsSection = getStatisticsTable(wholeWidth, scores.get(0), averages.get(0), highests.get(0), lowests.get(0));
			document.add(statsSection);

			// 6. branch section
			Table branchNote = new Table(new float[]{wholeWidth});
			branchNote.addCell(detailCell(branch.getName() + " (" + branch.getPhone()+ ")").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			document.add(branchNote);
		
			// Add a page break
			document.add(new AreaBreak());

			// 7. history title section
			Table historyTitle = new Table(new float[]{wholeWidth});
			historyTitle.addCell(detailCell(testTitles.get(0) + " Past Average & Your Scores").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			document.add(historyTitle);
			
			// 8. history section
			boolean isMegaOrRevision = false;
			if (StringUtils.startsWithIgnoreCase(testGroupName, "Mega") || StringUtils.startsWithIgnoreCase(testGroupName, "Revision")) {
				isMegaOrRevision = true;
			}
			if(isMegaOrRevision){
				Table historySection1 = getHistoryTopTable(wholeWidth, histories.get(0), 6);
				document.add(historySection1);
				document.add(onespace);
			}else{
				List<TestResultHistoryDTO> dummy = getDummyHistory();
				Table historySection1 = getHistoryTopTable(wholeWidth, dummy, 15);
				document.add(historySection1);
				// Table historySection2 = getHistoryBottomTable(wholeWidth, dummy);
				// historySection2.setMarginTop(1);
				// document.add(historySection2);
				document.add(onespace);
			}

			
			// 9. history graph section
			Table historyGraph = getHistoryGraphTable(wholeWidth, histories.get(0));
			document.add(historyGraph);
			
			document.close();

			byte[] pdfData = baos.toByteArray();
			return pdfData;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] dummyPdf(StudentDTO student) {
		try {
			// // Set the content type and attachment header.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter pdfWriter = new PdfWriter(baos);
			PdfDocument pdfDocument = new PdfDocument(pdfWriter);
			pdfDocument.setDefaultPageSize(PageSize.A4);
			Document document = new Document(pdfDocument);
			Paragraph onespace = new Paragraph("\n");
			float wholeWidth = pdfDocument.getDefaultPageSize().getWidth(); // whole width

			// 1. student section
			// Table header = getStudentTable(wholeWidth, "Scholarship Trial Test", student);
			// document.add(header);
			// document.add(onespace);

			// 2. title section
			// Table totalScore = new Table(new float[]{wholeWidth});
			// totalScore.addCell(detailCell("You have scored 19 out of 40 (48%)").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			// document.add(totalScore);

			// // 3. answer section
			// Table detailScore = getDetailAnswer(wholeWidth);
			// document.add(detailScore);
			// document.add(onespace);

			// 4. statistics title section
			// Table statsTitle = new Table(new float[]{wholeWidth});
			// statsTitle.addCell(detailCell("Scholarship Trial Test - Y6 Humanities Test 19 (Acer) Result").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			// document.add(statsTitle);

			// 5. statistics section
			// Table statsSection = getStatisticsTable(wholeWidth);
			// document.add(statsSection);

			// 6. branch section
			// Table branchNote = new Table(new float[]{wholeWidth});
			// branchNote.addCell(detailCell("Glen Waverley (8521 3786)").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			// document.add(branchNote);
			
			// 7. history title section
			// Table historyTitle = new Table(new float[]{wholeWidth});
			// historyTitle.addCell(detailCell("ENGLISH Past Average & Your Scores").setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
			// document.add(historyTitle);
			
			// 8. history section
			// List<TestResultHistoryDTO> dummy = getDummyHistory();
			// Table historySection1 = getHistoryTopTable(wholeWidth, dummy);
			// document.add(historySection1);
			// Table historySection2 = getHistoryBottomTable(wholeWidth, dummy);
			// historySection2.setMarginTop(1);
			// document.add(historySection2);
			// document.add(onespace);
			
			// 9. history graph section
			// Table historyGraph = getHistoryGraphTable(wholeWidth, dummy);
			// document.add(historyGraph);
			
			document.close();

			byte[] pdfData = baos.toByteArray();
			return pdfData;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// 1. student section
	private Table getStudentTable(float width, String title, String testInfo, StudentDTO student){ 
		Table note = new Table(new float[]{width});
		note.addCell(detailCell(title).setItalic().setFontSize(10f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
		note.addCell(detailCell("Dear " + student.getFirstName() + " " + student.getLastName() + " (" + student.getId()+ ")").setFontSize(8f).setBold().setBorder(Border.NO_BORDER));
		note.addCell(detailCell("Thank you for participating in the JAC " + title).setFontSize(8f).setBold().setBorder(Border.NO_BORDER));
		note.addCell(detailCell(JaeUtils.getGradeName(student.getGrade()) + " " + testInfo).setFontSize(8f).setBold().setBorder(Border.NO_BORDER));
		return note;
	}

	// 3. answer section
	private Table getDetailAnswer(float width, List<Integer> studentAnswers, List<TestAnswerItem> testAnswers) {
		Table body = new Table(new float[]{(width / 2), (width / 2)});
		if (testAnswers == null || testAnswers.isEmpty()) {
			return body;
		}
	
		int size = testAnswers.size();
		int splitSize = 30;	
		if (size <= splitSize) {
			// If size is less than or equal to 30, use only the left table
			Table left = getLeftDetailScore(width, studentAnswers, testAnswers);
			body.addCell(new Cell().add(left).setBorder(Border.NO_BORDER));
			body.addCell(new Cell().setBorder(Border.NO_BORDER)); // Empty right cell
		} else {
			// Split testAnswers into left and right parts
			List<Integer> leftStudentAnswers = studentAnswers.subList(0, splitSize);
			List<Integer> rightStudentAnswers = studentAnswers.subList(splitSize, size);
			List<TestAnswerItem> leftAnswers = testAnswers.subList(0, splitSize);
			List<TestAnswerItem> rightAnswers = testAnswers.subList(30, splitSize);
	
			// Generate left and right tables
			Table left = getLeftDetailScore(width, leftStudentAnswers, leftAnswers);
			Table right = getRightDetailScore(width, rightStudentAnswers, rightAnswers);
	
			// Add left and right tables to the body
			body.addCell(new Cell().add(left).setBorder(Border.NO_BORDER));
			body.addCell(new Cell().add(right).setBorder(Border.NO_BORDER));
		}
	
		return body;
	}

	// 5. stats section
	private Table getStatisticsTable(float width, double score, double average, double highest, double lowest) {
		Table body = new Table(new float[]{(width/2), (width/2)});
		Image left = getLeftBarChart(width, (int) score, (int) average, (int) highest, (int) lowest);
		Image right = getRightBarChart(width, (int) score, (int) average, (int) highest, (int) lowest);
		body.addCell(new Cell().add(left).setBorder(Border.NO_BORDER));
		body.addCell(new Cell().add(right).setBorder(Border.NO_BORDER));
		return body;
	}

	// 8. history section
	private Table getHistoryTopTable(float width, List<TestResultHistoryDTO> histories, int num){
		// dynamic width based on num
		float[] columnWidths = new float[num];
		for (int i = 0; i < num; i++) {
			if (i == 0) {
				columnWidths[i] = (width / num) * 2; // First column is wider
			} else {
				columnWidths[i] = width / num; // Other columns have equal width
			}
		}
		Table details = new Table(columnWidths);		
		// Define header background color
    	DeviceRgb headerBgColor = new DeviceRgb(200, 200, 200); // Light gray
		details.addCell(detailCell("Test No").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		for (int i = 1; i < num; i++) {
			details.addCell(detailCell(String.valueOf(i)).setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		}
		// first row
		Cell cell1_1 = detailCell("Your Score").setBold().setTextAlignment(TextAlignment.CENTER);
		details.addCell(cell1_1);
		// loop through the history if testID from 1 to 14
		for (int i = 1; i < num; i++) {
			String studentScore = "";
			for (TestResultHistoryDTO history : histories) {
				if (history.getTestNo() == i) {
					studentScore = history.getStudentScore() > 0 ? String.valueOf(history.getStudentScore()) : "";
					break;
				}
			}
			Cell cell = detailCell(studentScore).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell);
		}		
		// second row
		Cell cell2_1 = detailCell("Average").setBold().setTextAlignment(TextAlignment.CENTER);
		details.addCell(cell2_1);
		// loop through the history if testID from 1 to 14
		for (int i = 1; i < num; i++) {
			String average = "";
			for (TestResultHistoryDTO history : histories) {
				if (history.getTestNo() == i) {
					average = history.getAverage() > 0 ? String.valueOf(history.getAverage()) : "";
					break;
				}
			}
			Cell cell = detailCell(average).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell);
		}	
		return details;		
	}

	// 8. history section
	private Table getHistoryBottomTable(float width, List<TestResultHistoryDTO> histories){
		Table details = new Table(new float[]{((width)/15*2), (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15, (width)/15,});
		// Define header background color
    	DeviceRgb headerBgColor = new DeviceRgb(200, 200, 200); // Light gray
		details.addCell(detailCell("Test No").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("15").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("16").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("17").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("18").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("19").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("20").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("21").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("22").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("23").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("24").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("25").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("26").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("27").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("28").setBold().setBackgroundColor(headerBgColor)).setTextAlignment(TextAlignment.CENTER);
		// first row
		Cell cell1_1 = detailCell("Your Score").setBold().setTextAlignment(TextAlignment.CENTER);
		details.addCell(cell1_1);
		// loop through the history if testID from 1 to 14
		for(int i=15; i<29; i++){
			// int testNo = i;
			String studentScore = "";
			for(TestResultHistoryDTO history : histories){
				if(history.getTestNo() == i){
					studentScore = history.getStudentScore() > 0 ? String.valueOf(history.getStudentScore()) : "";
					break;
				}
			}
			Cell cell = detailCell(studentScore).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell);
			// System.out.println(testNo + " " + studentScore);
		}
		// second row
		Cell cell2_1 = detailCell("Average").setBold().setTextAlignment(TextAlignment.CENTER);
		details.addCell(cell2_1);
		// loop through the history if testID from 1 to 14
		for(int i=15; i<29; i++){
			// int testNo = i;
			String average = "";
			for(TestResultHistoryDTO history : histories){
				if(history.getTestNo() == i){
					// average = String.valueOf(history.getAverage());
					average = history.getAverage() > 0 ? String.valueOf(history.getAverage()) : "";
					break;
				}
			}
			Cell cell = detailCell(average).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell);
			// System.out.println(testNo + " " + average);
		}
		return details;
		
	}

	// 9. history graph section
	private Table getHistoryGraphTable(float width, List<TestResultHistoryDTO> histories){
		Table body = new Table(new float[]{(width)});
		Image line = getLineChart(width, histories);
		body.addCell(new Cell().add(line).setBorder(Border.NO_BORDER));
		return body;
	}

	// get left bar chart
	private Image getLeftBarChart(float width, int score, int average, int highest, int lowest) {
		// Create a chart and add it to the PDF
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String seriesName = "Scores"; 

		dataset.addValue(score, seriesName, "Your Mark");
		dataset.addValue(average, seriesName, "Average");
		dataset.addValue(highest, seriesName, "Highest");
		dataset.addValue(lowest, seriesName, "Lowest");
		
		JFreeChart barChart = ChartFactory.createBarChart(
				"",
				null,
				null,
				dataset,
				PlotOrientation.VERTICAL,
				true, true, false);

		barChart.setBackgroundPaint(Color.WHITE);
		barChart.getTitle().setPaint(Color.BLACK);

		// Enable anti-aliasing
		barChart.setAntiAlias(true);
		barChart.setTextAntiAlias(true);
		barChart.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	 
		// Customize the plot
		CategoryPlot plot = (CategoryPlot) barChart.getPlot();
		plot.setBackgroundPaint(Color.WHITE); // Set plot background color
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);

		// Set the range axis (Y axis) to display up to 100%
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100.0);
		rangeAxis.setTickUnit(new NumberTickUnit(25));

		// Set the font size for the range axis (Y axis)
		rangeAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
		rangeAxis.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
		
		// Set the font size for the domain axis (X axis)
		plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
		plot.getDomainAxis().setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));

		// Create and set custom renderer
		BarRenderer renderer = new BarRenderer() {
			@Override
			public Paint getItemPaint(int row, int column) {
				switch (column) {
					case 0: return Color.decode("#033781"); // "Your Mark" - Navy
					case 1: return Color.decode("#ADD8E6"); // "Average" - Light Blue
					case 2: return Color.decode("#ADD8E6"); // "Highest" - Light Blue
					case 3: return Color.decode("#ADD8E6"); // "Lowest" - Light Blue
					default: return super.getItemPaint(row, column);
				}
			}
		};

		// Apply to plot
		plot.setRenderer(renderer);
		// Remove decorations
		renderer.setShadowVisible(false); // Disable shadows
		renderer.setBarPainter(new StandardBarPainter()); // Remove gradient effects
		renderer.setDrawBarOutline(true); // Keep bar outlines for clarity
		renderer.setSeriesOutlinePaint(0, Color.BLACK); // Black outline for bars
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f)); // Define outline thickness

		// Format labels as percentages
		NumberFormat percentFormat = NumberFormat.getNumberInstance();
		percentFormat.setMaximumFractionDigits(0);
		StandardCategoryItemLabelGenerator labelGenerator = new StandardCategoryItemLabelGenerator("{2}%", percentFormat);

		// Apply label generator
		renderer.setDefaultItemLabelGenerator(labelGenerator);
		renderer.setDefaultItemLabelsVisible(true);

		// Adjust bar width
		renderer.setMaximumBarWidth(0.15);

		// Set the outline paint and stroke for the border of series 1
		renderer.setSeriesOutlinePaint(1, Color.black); // Set the border color
		renderer.setSeriesOutlineStroke(1, new BasicStroke(1.0f)); // Set the border thickness
		renderer.setDrawBarOutline(true); // Enable drawing the outline

		// Set the outline paint and stroke for the border of series 0 (if needed)
		renderer.setSeriesOutlinePaint(0, Color.black); // Set the border color for series 0
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f)); // Set the border thickness for series 0
		renderer.setDrawBarOutline(true); // Enable drawing the outline

		// Set the font size for the legend
		barChart.removeLegend();
		ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
		try {
			// Adjust the width and height proportionally
			int chartWidth = (int) (width / 2.25);
			int chartHeight = (int) (chartWidth * 0.7); // Adjust height to maintain aspect ratio
			ChartUtils.writeChartAsPNG(chartOutputStream, barChart, chartWidth, chartHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageData imageData = ImageDataFactory.create(chartOutputStream.toByteArray());
		Image chartImage = new Image(imageData);
		// Center horizontally
		chartImage.setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.CENTER);
		return chartImage;
	}

	// get right bar chart
	private Image getRightBarChart(float width, int score, int average, int highest, int lowest) {
		// Create dataset
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(23, "Scores", "Lowest");
		dataset.addValue(17, "Scores", "Lower");
		dataset.addValue(20, "Scores", "Middle");
		dataset.addValue(12, "Scores", "Above");
		dataset.addValue(17, "Scores", "Higher");
		dataset.addValue(11, "Scores", "Top");
	
		// Create the chart
		JFreeChart barChart = ChartFactory.createBarChart(
				"",
				null,
				null,
				dataset,
				PlotOrientation.VERTICAL,
				true, true, false
		);
	
		// Customize chart background
		barChart.setBackgroundPaint(Color.WHITE);
		barChart.setAntiAlias(true);
		barChart.setTextAntiAlias(true);
	
		// Customize plot
		CategoryPlot plot = (CategoryPlot) barChart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
	
		// Configure range axis (Y-axis)
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100.0);
		rangeAxis.setTickUnit(new NumberTickUnit(25));
		rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 8));
	
		// Configure category axis (X-axis)
		plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 8));

		// calculate the band for the score
		String studentCategory = getBandForScore(score, lowest, highest);
        //System.out.println("Student score " + score + "% falls into category: " + studentCategory);
	
		// Custom renderer to apply different colors and display percentages
		BarRenderer renderer = new BarRenderer() {
			@Override
			public Paint getItemPaint(int row, int column) {
				// Get the category name for the current column
				String category = (String) dataset.getColumnKey(column);
				if (category.equalsIgnoreCase(studentCategory)) {
					return Color.decode("#033781"); // Highlight matching category
				} else {
					return Color.decode("#ADD8E6"); // Default Light Blue
				}
			}
		};
	
		// Remove gradient effects
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
	
		// Set border color and thickness
		renderer.setDrawBarOutline(true);
		renderer.setSeriesOutlinePaint(0, Color.BLACK);
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
	
		// Format labels to display as percentages
		NumberFormat percentFormat = NumberFormat.getNumberInstance();
		percentFormat.setMaximumFractionDigits(0);
		StandardCategoryItemLabelGenerator labelGenerator = new StandardCategoryItemLabelGenerator("{2}%", percentFormat);
	
		// Apply label generator
		renderer.setDefaultItemLabelGenerator(labelGenerator);
		renderer.setDefaultItemLabelsVisible(true);
	
		// Adjust bar width
		renderer.setMaximumBarWidth(0.15);
	
		// Apply renderer to plot
		plot.setRenderer(renderer);
	
		// Remove legend
		barChart.removeLegend();
	
		// Convert chart to an image
		ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
		try {
			int chartWidth = (int) (width / 2.25);
			int chartHeight = (int) (chartWidth * 0.7);
			ChartUtils.writeChartAsPNG(chartOutputStream, barChart, chartWidth, chartHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		// Create and return image
		ImageData imageData = ImageDataFactory.create(chartOutputStream.toByteArray());
		Image chartImage = new Image(imageData);
		chartImage.setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.CENTER);
		return chartImage;
	}

	// draw line chart
	private Image getLineChart(float width, List<TestResultHistoryDTO> histories) {
		// Create dataset
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 1; i < 29; i++) {
			String testNo = String.valueOf(i);
			int studentScore = 0;
			int average = 0;
			for (TestResultHistoryDTO history : histories) {
				if (history.getTestNo() == i) {
					studentScore = history.getStudentScore();
					average = history.getAverage();
					break;
				}
			}
			if (studentScore > 0) {
				dataset.addValue(studentScore, "Your Score", testNo);
			}
			if (average > 0) {
				dataset.addValue(average, "Average", testNo);
			}
		}
		// Create Line Chart
		JFreeChart lineChart = ChartFactory.createLineChart(
				"", // Chart title
				"", // X-Axis Label
				"Score", // Y-Axis Label
				dataset,
				PlotOrientation.VERTICAL,
				true, true, false);
	
		// Set chart background
		lineChart.setBackgroundPaint(Color.WHITE);
		// Customize the plot
		CategoryPlot plot = (CategoryPlot) lineChart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		// Customize axis
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100.0); // Keep the range between 0 to 100
		rangeAxis.setTickUnit(new NumberTickUnit(10)); // Show every 10%
		rangeAxis.setNumberFormatOverride(new NumberFormat() {
			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
				return toAppendTo.append((int) number).append("%"); // Append '%' correctly
			}

			@Override
			public Number parse(String source, ParsePosition parsePosition) {
				return null;
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'format'");
			}
		});
		rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
		rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
		rangeAxis.setLabel("");
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
		domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
		// Customize renderer
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.decode("#ADD8E6")); // Average
		renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0)); // Dashed line
		renderer.setSeriesShapesVisible(0, true);
		renderer.setSeriesPaint(1, Color.decode("#033781")); // Your Score 
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		renderer.setSeriesShapesVisible(1, true);
		renderer.setSeriesItemLabelFont(1, new Font("SansSerif", Font.BOLD, 10));

		// Add item labels to show scores only for "Your Score"
		// Custom Label Generator to Append '%' to "Your Score"
		renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator() {
			@Override
			public String generateLabel(CategoryDataset dataset, int row, int column) {
				Number value = dataset.getValue(row, column);
				if (value != null) {
					return value.intValue() + "%"; // Append '%' to each value
				}
				return "";
			}
		});
		renderer.setSeriesItemLabelsVisible(0, true);
		renderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 10));
		renderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
	
		plot.setRenderer(renderer);
		// Convert chart to image
		ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
		try {
			int chartWidth = (int) (width / 1.15);
			int chartHeight = (int) (chartWidth * 0.5);
			ChartUtils.writeChartAsPNG(chartOutputStream, lineChart, chartWidth, chartHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageData imageData = ImageDataFactory.create(chartOutputStream.toByteArray());
		Image chartImage = new Image(imageData);
		chartImage.setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.CENTER);
		return chartImage;
	}

	// left answer from 1 ~ 30
	private Table getLeftDetailScore(float width, List<Integer> studentAnswers, List<TestAnswerItem> testAnswers){ 
		Table subject = new Table(new float[]{(width/2)});
		Table details = new Table(new float[]{(width/2)/10, ((width/2)/10)*2, (width/2)/10, ((width/2)/10)*2, ((width/2)/10)*4});
		details.addCell(detailCell("Q.No").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Resp").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Ans").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Percent").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Topic").setBold().setBorder(Border.NO_BORDER));
		int count1 = testAnswers.size();
		for(int i=0; i< count1; i++){
			// background color
			com.itextpdf.kernel.color.Color backgroundColor = (i % 2 == 0) ? com.itextpdf.kernel.color.Color.LIGHT_GRAY : com.itextpdf.kernel.color.Color.WHITE;
			Cell cell1 = detailCell(testAnswers.get(i).getQuestion() + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setBold().setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell1);
			char mark = testAnswers.get(i).getAnswer() == studentAnswers.get(i) ? 'O' : 'X';			
			Cell cell2 = detailCell(formatAnswer(testAnswers.get(i).getAnswer()) + " - " + mark).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell2);			
			Cell cell3 = detailCell(formatAnswer(studentAnswers.get(i))+"").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell3);			
			Cell cell4 = detailCell("57").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER); // percent
			details.addCell(cell4);			
			Cell cell5 = detailCell(testAnswers.get(i).getTopic()).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.LEFT); // topics
			details.addCell(cell5);
		}
		subject.addCell(details.setMarginTop(1));
		return subject;
	}

	// right answer from question 31 ~
	private Table getRightDetailScore(float width, List<Integer> studentAnswers, List<TestAnswerItem> testAnswers){ 
		Table subject = new Table(new float[]{(width/2)});
		Table details = new Table(new float[]{(width/2)/10, ((width/2)/10)*2, (width/2)/10, ((width/2)/10)*2, ((width/2)/10)*4});
		details.addCell(detailCell("Q.No").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Resp").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Ans").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Percent").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Topic").setBold().setBorder(Border.NO_BORDER));	
		int count2 = testAnswers.size();
		for(int i=0; i< count2; i++){
			// background color
			com.itextpdf.kernel.color.Color backgroundColor = (i % 2 == 0) ? com.itextpdf.kernel.color.Color.LIGHT_GRAY : com.itextpdf.kernel.color.Color.WHITE;
			Cell cell1 = detailCell(testAnswers.get(i).getQuestion() + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setBold().setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell1);
			char mark = testAnswers.get(i).getAnswer() == studentAnswers.get(i) ? 'O' : 'X';			
			Cell cell2 = detailCell(formatAnswer(testAnswers.get(i).getAnswer()) + " - " + mark).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);	            
			details.addCell(cell2);            
			Cell cell3 = detailCell(formatAnswer(studentAnswers.get(i))+"").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell3);            
			Cell cell4 = detailCell("31").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER); // percent
			details.addCell(cell4);            
			Cell cell5 = detailCell(testAnswers.get(i).getTopic()).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.LEFT); // topics
			details.addCell(cell5);
		}
		subject.addCell(details.setMarginTop(1).setBorder(Border.NO_BORDER));
		subject.setBorder(Border.NO_BORDER);
		return subject;
	}



	private List<TestResultHistoryDTO> getDummyHistory(){
		List<TestResultHistoryDTO> list = new ArrayList<>();
		for(int i=5; i<10; i++){
			TestResultHistoryDTO dto = new TestResultHistoryDTO();
			dto.setTestNo(i);
			// random number from 20~99
			dto.setAverage(new Random().nextInt(80) + 20);
			list.add(dto);
		}
		for(int i=10; i<=20; i++){
			TestResultHistoryDTO dto = new TestResultHistoryDTO();
			dto.setTestNo(i);
			// random number from 20~99
			dto.setAverage(new Random().nextInt(80) + 20);
			dto.setStudentScore(new Random().nextInt(80) + 20);
			list.add(dto);
		}
		return list;
	}












































	@Override
	public byte[] generateAssessmentPdf(Map<String, Object> data) {
		try {
			// // Set the content type and attachment header.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter pdfWriter = new PdfWriter(baos);
			PdfDocument pdfDocument = new PdfDocument(pdfWriter);
			pdfDocument.setDefaultPageSize(PageSize.A4);
			Document document = new Document(pdfDocument);
			Paragraph onespace = new Paragraph("\n");
			Paragraph halfspace = new Paragraph(" ").setFixedLeading(5).setVerticalAlignment(VerticalAlignment.MIDDLE).setMargin(0);
			float wholeWidth = pdfDocument.getDefaultPageSize().getWidth(); // whole width
			float wholeHeight = pdfDocument.getDefaultPageSize().getHeight(); // whole height
			
			// prepare ingredients
			GuestStudent student = (GuestStudent) data.get(JaeConstants.STUDENT_INFO);
			List<GuestStudentAssessmentDTO> gsas = (List<GuestStudentAssessmentDTO>) data.get(JaeConstants.STUDENT_ANSWER);
			List<AssessmentAnswerDTO> aas = (List<AssessmentAnswerDTO>) data.get(JaeConstants.CORRECT_ANSWER);
			
			// 1. button section
			Image header = imageLogo();
			float x = wholeWidth/2 - 250;
			float y = wholeHeight/2 + 330;

			header.setFixedPosition(x, y);
			document.add(header);
			document.add(onespace);
			// document.add(onespace);
			document.add(onespace);

			// Date
			Cell dateCell = new Cell().add("Test Date : " + JaeUtils.convertToddMMyyyyFormat(student.getRegisterDate()+"")).setFontSize(10f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginRight(100);
			document.add(dateCell);
			
			// 2. student info section
			Table title = getStudentTable(wholeWidth, student, gsas);
			document.add(halfspace);
			document.add(title);
			document.add(halfspace);

			// 3. answer section// // 4. header section
			Table answer = getAnswerTable(wholeWidth, gsas, aas);
			document.add(answer);

			// 4. branch note section
			Table note = getBranchNoteTable(wholeWidth, student);
			document.add(note);
			document.close();


			byte[] pdfData = baos.toByteArray();
			return pdfData;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void generateTestPdf(Map<String, Object> data) {
		try {
			// pdf directory - resources/pdf
			String projectRootPath = new File("").getAbsolutePath();
			String pdfDirectoryPath = projectRootPath + "/src/main/resources/pdf/";
			File pdfDirectory = new File(pdfDirectoryPath);
			if (!pdfDirectory.exists()) {
				pdfDirectory.mkdirs();
			}
			String fullPath = pdfDirectoryPath + "test.pdf";

			PdfWriter pdfWriter = new PdfWriter(fullPath);
			PdfDocument pdfDocument = new PdfDocument(pdfWriter);
			pdfDocument.setDefaultPageSize(PageSize.A4);
			Document document = new Document(pdfDocument);
			Paragraph onespace = new Paragraph("\n");
			Paragraph halfspace = new Paragraph(" ").setFixedLeading(5).setVerticalAlignment(VerticalAlignment.MIDDLE).setMargin(0);
			float wholeWidth = pdfDocument.getDefaultPageSize().getWidth(); // whole width
			float wholeHeight = pdfDocument.getDefaultPageSize().getHeight(); // whole height
			
			// prepare ingredients
			GuestStudent student = (GuestStudent) data.get(JaeConstants.STUDENT_INFO);
			List<GuestStudentAssessmentDTO> gsas = (List<GuestStudentAssessmentDTO>) data.get(JaeConstants.STUDENT_ANSWER);
			List<AssessmentAnswerDTO> aas = (List<AssessmentAnswerDTO>) data.get(JaeConstants.CORRECT_ANSWER);
			
			// 1. button section
			Image header = imageLogo();
			float x = wholeWidth/2 - 250;
			float y = wholeHeight/2 + 330;

			header.setFixedPosition(x, y);
			document.add(header);
			document.add(onespace);
			// document.add(onespace);
			document.add(onespace);

			// Date
			Cell dateCell = new Cell().add("Test Date : " + JaeUtils.convertToddMMyyyyFormat(student.getRegisterDate()+"")).setFontSize(10f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginRight(100);
			document.add(dateCell);
			
			// 2. student info section
			Table title = getStudentTable(wholeWidth, student, gsas);
			document.add(halfspace);
			document.add(title);
			document.add(halfspace);

			// 3. answer section// // 4. header section
			Table answer = getAnswerTable(wholeWidth, gsas, aas);
			document.add(answer);

			// 4. branch note section
			Table note = getBranchNoteTable(wholeWidth, student);
			document.add(note);
			document.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// student info section
	private Table getStudentTable(float wholeWidth, GuestStudent guest, List<GuestStudentAssessmentDTO> gsas){
		int subjectCount = gsas.size();
		double total = 0;
		for(GuestStudentAssessmentDTO dto : gsas){
			total += dto.getScore();
		}
		double average = total/subjectCount;
		Table table = new Table(new float[]{wholeWidth});
		table.addCell(studentCell("Dear " + guest.getFirstName() + " " + guest.getLastName()).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)).setItalic();
		table.addCell(studentCell("Thank you for participating in the James An College " + JaeUtils.getGradeYearName(guest.getGrade()) + " Assessment Test.").setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)).setItalic();
		table.addCell(studentCell("Your marks are indicated below in detail. (Average score for " + subjectCount + " subject/s : " + String.format("%.2f",average) + "%)").setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)).setItalic();
		return table;
	}

	// answer section
	private Table getAnswerTable(float wholeWidth, List<GuestStudentAssessmentDTO> gsas, List<AssessmentAnswerDTO> aas){ 	
		Table body = new Table(new float[]{(wholeWidth/2), (wholeWidth/2)});
		double englishScore = 0;
		double mathScore = 0;
		double gaScore = 0;
		// English
		for(GuestStudentAssessmentDTO dto : gsas){
			if(JaeConstants.SUBJECT_ENGLISH.equalsIgnoreCase(StringUtils.defaultString(dto.getSubject()))){
				englishScore = dto.getScore();
				long assessmentId = dto.getAssessmentId();
				AssessmentAnswerDTO answer = null;
				for(AssessmentAnswerDTO aa : aas){
					if(aa.getAssessmentId() == assessmentId){
						answer = aa;
						break;
					}
				}
				Table subject = getSubjectTable(wholeWidth, dto, answer);
				body.addCell(new Cell().add(subject).setBorder(Border.NO_BORDER));
			}						
		}
		// Math
		for(GuestStudentAssessmentDTO dto : gsas){
			if(JaeConstants.SUBJECT_MATH.equalsIgnoreCase(StringUtils.defaultString(dto.getSubject()))){
				mathScore = dto.getScore();
				long assessmentId = dto.getAssessmentId();
				AssessmentAnswerDTO answer = null;
				for(AssessmentAnswerDTO aa : aas){
					if(aa.getAssessmentId() == assessmentId){
						answer = aa;
						break;
					}
				}
				Table subject = getSubjectTable(wholeWidth, dto, answer);
				body.addCell(new Cell().add(subject).setBorder(Border.NO_BORDER));
			}						
		}
		// GA
		boolean gaExist = false;
		for(GuestStudentAssessmentDTO dto : gsas){
			if(JaeConstants.SUBJECT_GA.equalsIgnoreCase(StringUtils.defaultString(dto.getSubject()))){
				gaExist = true;
				break;
			}
		}
		if(gaExist){
			for(GuestStudentAssessmentDTO dto : gsas){
				if(JaeConstants.SUBJECT_GA.equalsIgnoreCase(StringUtils.defaultString(dto.getSubject()))){
					gaExist = true;
					gaScore = dto.getScore();
					long assessmentId = dto.getAssessmentId();
					AssessmentAnswerDTO answer = null;
					for(AssessmentAnswerDTO aa : aas){
						if(aa.getAssessmentId() == assessmentId){
							answer = aa;
							break;
						}
					}
					Table subject = getSubjectTable(wholeWidth, dto, answer);
					body.addCell(new Cell().add(subject).setBorder(Border.NO_BORDER));
				}						
			}
		}else{ // GA does not exist
			Table subject = getEmptySubjectTable(wholeWidth);
			body.addCell(new Cell().add(subject).setBorder(Border.NO_BORDER));
		}

		// Sample data
		Map<String, Double> studentMarks = Map.of(JaeConstants.SUBJECT_ENGLISH, englishScore, JaeConstants.SUBJECT_MATH, mathScore, JaeConstants.SUBJECT_GA_SHORT, gaScore);
		Map<String, Double> averageMarks = Map.of(JaeConstants.SUBJECT_ENGLISH, 59.5, JaeConstants.SUBJECT_MATH, 58.4, JaeConstants.SUBJECT_GA_SHORT, (gaScore==0) ? 0.0 : 60);
		// Create a chart and add it to the PDF
        JFreeChart barChart = createChart(studentMarks, averageMarks);
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        try {
			// Adjust the width and height proportionally
			int chartWidth = (int) (wholeWidth / 2.65);
			int chartHeight = (int) (chartWidth * 1.25);//(int) (chartWidth * 0.75); // Adjust height to maintain aspect ratio
			ChartUtils.writeChartAsPNG(chartOutputStream, barChart, chartWidth, chartHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
        ImageData imageData = ImageDataFactory.create(chartOutputStream.toByteArray());
        Image chartImage = new Image(imageData);
		// Center horizontally
        chartImage.setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.CENTER);
        // Position at the bottom
        float x = 310;
        float y = 105; // Set the Y position to the bottom margin
        chartImage.setFixedPosition(x, y);
        // Add content to the document
        body.addCell(chartImage);
		return body;
	}

	private Table getSubjectTable(float wholeWidth, GuestStudentAssessmentDTO dto, AssessmentAnswerDTO answer) {
		@SuppressWarnings("null")
		int count = answer.getAnswers().size();

		Table subject = new Table(new float[]{(wholeWidth/2)});
		Table details = new Table(new float[]{(wholeWidth/2)/10, (wholeWidth/2)/10, (wholeWidth/2)/10, (wholeWidth/2)/10, ((wholeWidth/2)/10)*2, ((wholeWidth/2)/10)*4});
		details.addCell(detailCell("Qn").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Resp").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Ans").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Correct").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Percent").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Topics").setBold().setBorder(Border.NO_BORDER));

		int correctCnt = 0;
		for (int i = 0; i < count; i++) {
			int resp = dto.getAnswers().get(i);
			int ans = answer.getAnswers().get(i).getAnswer();
			String correct = (resp == ans) ? "O" : "X";
			correctCnt += (resp == ans) ? 1 : 0;
			String percent = String.valueOf(55 + new java.util.Random().nextInt(19) - 9) + "%" ;
			
			String topics = answer.getAnswers().get(i).getTopic();
			// background color
			com.itextpdf.kernel.color.Color backgroundColor = (i % 2 == 0) ? com.itextpdf.kernel.color.Color.LIGHT_GRAY : com.itextpdf.kernel.color.Color.WHITE;
			Cell cell1 = detailCell((i + 1) + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setBold().setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell1);			
			Cell cell2 = detailCell(JaeUtils.getAnswer(resp) + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell2);			
			Cell cell3 = detailCell(JaeUtils.getAnswer(ans) + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell3);			
			Cell cell4 =  (correct.equals("O")) ?  detailCell(correct).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER).setFontColor(com.itextpdf.kernel.color.Color.BLUE) : detailCell(correct).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER).setFontColor(com.itextpdf.kernel.color.Color.BLACK);
			details.addCell(cell4);			
			Cell cell5 = detailCell(percent).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER); // percent
			details.addCell(cell5);			
			Cell cell6 = detailCell(topics).setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.LEFT); // topics
			details.addCell(cell6);
		}
		// Your existing code
		Cell subjectCell = new Cell();
		subjectCell.add(dto.getSubject().toUpperCase() + " : " + (int)(dto.getScore()) + "% (" + correctCnt + " out of " + count + ")");
		subjectCell.setFontSize(9.5f);
		subjectCell.setBorder(Border.NO_BORDER);
		// Set background color to black
		subjectCell.setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK);
		// Set font color to white
		subjectCell.setFontColor(com.itextpdf.kernel.color.Color.WHITE);
		// Set text alignment to center
		subjectCell.setTextAlignment(TextAlignment.CENTER);
		subjectCell.setBold();
		// Add cell to your table or document
		subject.addCell(subjectCell);
		subject.addCell(details.setMarginTop(1));
		return subject;
	}

	private Table getEmptySubjectTable(float wholeWidth) {
		@SuppressWarnings("null")
		int count = 20;

		Table subject = new Table(new float[]{(wholeWidth/2)});
		Table details = new Table(new float[]{(wholeWidth/2)/10, (wholeWidth/2)/10, (wholeWidth/2)/10, (wholeWidth/2)/10, ((wholeWidth/2)/10)*2, ((wholeWidth/2)/10)*4});
		details.addCell(detailCell("Qn").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Resp").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Ans").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Correct").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Percent").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Topics").setBold().setBorder(Border.NO_BORDER));

		for (int i = 0; i < count; i++) {
			com.itextpdf.kernel.color.Color backgroundColor = (i % 2 == 0) ? com.itextpdf.kernel.color.Color.LIGHT_GRAY : com.itextpdf.kernel.color.Color.WHITE;
			Cell cell1 = detailCell("1").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setBold().setTextAlignment(TextAlignment.CENTER).setFontColor(backgroundColor);
			details.addCell(cell1);			
			Cell cell2 = detailCell("").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell2);			
			Cell cell3 = detailCell("").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell3);			
			Cell cell4 = detailCell("").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell4);			
			Cell cell5 = detailCell("").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell5);			
			Cell cell6 = detailCell("").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor);
			details.addCell(cell6);
		}
		// Your existing code
		Cell subjectCell = new Cell();
		subjectCell.add("General Ability".toUpperCase());
		subjectCell.setFontSize(9.5f);
		subjectCell.setBorder(Border.NO_BORDER);
		// Set background color to black
		subjectCell.setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK);
		// Set font color to white
		subjectCell.setFontColor(com.itextpdf.kernel.color.Color.WHITE);
		// Set text alignment to center
		subjectCell.setTextAlignment(TextAlignment.CENTER);
		// Add cell to your table or document
		subject.addCell(subjectCell);
		subject.addCell(details.setMarginTop(1));
		return subject;
	}
	

	// branch note section
	private Table getBranchNoteTable(float wholeWidth, GuestStudent guest) {
		Table note = new Table(new float[]{wholeWidth});
		note.addCell(detailCell("Thank you once again for taking part in James An College Assessment Test.").setItalic().setFontSize(7.5f).setBold().setBorder(Border.NO_BORDER));
		String branch = JaeUtils.getBranchName(guest.getBranch());
		note.addCell(detailCell(branch.toUpperCase() + " James An College").setItalic().setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setPaddingLeft(5));
		return note;
	}

	private Cell studentCell(String contents) {
		return new Cell().add(contents).setBold().setFontSize(7.5f);
	}

	private Cell detailCell(String contents) {
		return new Cell().add(contents).setFontSize(6.0f);
	}

	private Image imageLogo() throws URISyntaxException, MalformedURLException, IOException {
		Resource resource = resourceLoader.getResource("classpath:static/assets/image/logo_title.jpg");
		ImageData imageData = ImageDataFactory.create(resource.getFile().getAbsolutePath());
		Image img = new Image(imageData);
		img.setAutoScale(true);
		return img;
	}

	// get dataset for bar chart
	private CategoryDataset createDataset(Map<String, Double> studentMarks, Map<String, Double> averageMarks) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		studentMarks.forEach((subject, mark) -> {
			if (averageMarks.containsKey(subject) && averageMarks.get(subject) != 0) {
				dataset.addValue(mark, "Your Mark", subject);
				//dataset.addValue(averageMarks.get(subject), "Average Mark", subject);
				double diff = (mark - averageMarks.get(subject))/100;
				double average = averageMarks.get(subject)+diff;
				average = Math.round(average * 100.0) / 100.0;
				dataset.addValue(average, "Average Mark", subject);
			}
		});

		return dataset;
	}

	// create bar chart
	private JFreeChart createChart(Map<String, Double> studentMarks, Map<String, Double> averageMarks) {
        CategoryDataset dataset = createDataset(studentMarks, averageMarks);
        JFreeChart barChart = ChartFactory.createBarChart(
                "",
                null,
                null,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        barChart.setBackgroundPaint(Color.WHITE);
        barChart.getTitle().setPaint(Color.BLACK);
        
		// Enable anti-aliasing
		barChart.setAntiAlias(true);
		barChart.setTextAntiAlias(true);
		barChart.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	 
		// Customize the plot
		CategoryPlot plot = (CategoryPlot) barChart.getPlot();
		plot.setBackgroundPaint(Color.WHITE); // Set plot background color
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);

		// Set the range axis (Y axis) to display up to 100%
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100.0);
		rangeAxis.setTickUnit(new NumberTickUnit(20));

		// Set the font size for the range axis (Y axis)
		rangeAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
		rangeAxis.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
		
		// Set the font size for the domain axis (X axis)
		plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
		plot.getDomainAxis().setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));

		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDefaultItemLabelsVisible(true);
		renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
		renderer.setBarPainter(new StandardBarPainter());

		/*
		// Customize the color of the bars
		// renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setSeriesOutlinePaint(1, Color.decode("#033781")); // Set the border color to dark blue
		renderer.setSeriesPaint(1, Color.white);

		// Set the outline paint and stroke for the border
		renderer.setSeriesOutlinePaint(1, Color.black); // Set the border color
		renderer.setSeriesOutlineStroke(1, new BasicStroke(1.0f)); // Set the border thickness
		renderer.setDrawBarOutline(true); // Enable drawing the outline
		*/

		// Customize the color of the bars for series 0 and series 1
		renderer.setSeriesPaint(0, Color.decode("#033781")); // Assuming series 0 is for "Your Mark"
		renderer.setSeriesPaint(1, Color.white); // Assuming series 1 is for "Average Mark"

		// Set the outline paint and stroke for the border of series 1
		renderer.setSeriesOutlinePaint(1, Color.black); // Set the border color
		renderer.setSeriesOutlineStroke(1, new BasicStroke(1.0f)); // Set the border thickness
		renderer.setDrawBarOutline(true); // Enable drawing the outline

		// Set the outline paint and stroke for the border of series 0 (if needed)
		renderer.setSeriesOutlinePaint(0, Color.black); // Set the border color for series 0
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f)); // Set the border thickness for series 0
		renderer.setDrawBarOutline(true); // Enable drawing the outline




		// Set the font size for the legend
		barChart.getLegend().setItemFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));

		return barChart;
    }



	private String formatAnswer(int num){
		String answer = "";
		switch(num){
			case 1:
				answer = "A";
				break;
			case 2:
				answer = "B";
				break;
			case 3:
				answer = "C";
				break;
			case 4:
				answer = "D";
				break;
			case 5:
				answer = "E";
				break;
			default:
				answer = "";
		}
		return answer;
	}


/////////////////// Calculate band category ///////////////////////
	public static class Band {
		String name;
		double percentileCoverage;
		double scoreMin;
		double scoreMax;

		public Band(String name, double coverage) {
			this.name = name;
			this.percentileCoverage = coverage;
		}

		public boolean contains(double score) {
			return score >= scoreMin && score < scoreMax;
		}
	}

	// Step 1: Calculate the score range for each band
	private List<Band> calculateBands(double lowest, double highest) {
		List<Band> bands = List.of(
				new Band("Lowest", 23),
				new Band("Lower", 17),
				new Band("Middle", 20),
				new Band("Higher", 17),
				new Band("Above", 12),
				new Band("Top", 11)
		);

		double scoreSpan = highest - lowest;
		double cursor = lowest;

		for (Band band : bands) {
			double range = (band.percentileCoverage / 100.0) * scoreSpan;
			band.scoreMin = cursor;
			band.scoreMax = cursor + range;
			cursor = band.scoreMax;
		}

		return bands;
	}

	// Step 2: Find which band the student's score falls into
	private String getBandForScore(double studentScore, double lowest, double highest) {
		List<Band> bands = calculateBands(lowest, highest);
		for (Band band : bands) {
			if (band.contains(studentScore)) {
				return band.name;
			}
		}
		return "Top"; // If it's >= highest
	}

}
