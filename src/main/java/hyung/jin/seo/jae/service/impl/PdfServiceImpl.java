package hyung.jin.seo.jae.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import hyung.jin.seo.jae.dto.AssessmentAnswerDTO;
import hyung.jin.seo.jae.dto.GuestStudentAssessmentDTO;
import hyung.jin.seo.jae.model.GuestStudent;
import hyung.jin.seo.jae.service.PdfService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;

@Service
public class PdfServiceImpl implements PdfService {

	@Autowired
	private ResourceLoader resourceLoader;

	@Override
	public byte[] dummyPdf() {
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
			GuestStudent student = new GuestStudent();
			student.setFirstName("John");
			student.setLastName("Doe");
			student.setGrade("7");
			student.setRegisterDate(java.time.LocalDate.now());
			student.setBranch("13");
			student.setContactNo("0412345678");
			student.setEmail("abc@gmail.com");
			student.setState("1");
			student.setId(12345L);
			
			// 1. student section
			Table header = getStudentTable(wholeWidth, "Scholarship Trial Test", student);
			document.add(header);
			document.add(onespace);

			// 2. title section
			Table totalScore = getTitleTable(wholeWidth, "You have scored 19 out of 40 (48%) ");
			document.add(totalScore);

			// 3. answer section
			Table detailScore = getDetailAnswer(wholeWidth);
			document.add(detailScore);
			document.add(onespace);

			// 4. statistics title section
			Table statsTitle = getTitleTable(wholeWidth, "Scholarship Trial Test - Y6 Humanities Test 19 (Acer) Result");
			document.add(statsTitle);

			// 5. statistics section
			Table statsSection = getStatisticsTable(wholeWidth);
			document.add(statsSection);

			// 6. Branch section
			Table branchNote = getTitleTable(wholeWidth, "Glen Waverley (8521 3786)");
			document.add(branchNote);
			
			
			
			
			
			
			
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
	private Table getStudentTable(float width, String title, GuestStudent student){ 
		Table note = new Table(new float[]{width});
		note.addCell(detailCell(title).setItalic().setFontSize(10f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
		note.addCell(detailCell("Dear " + student.getFirstName() + " " + student.getLastName() + " (" + student.getId()+ ")").setFontSize(8f).setBold().setBorder(Border.NO_BORDER));
		note.addCell(detailCell("Thank you for participating in the JAC " + title).setFontSize(8f).setBold().setBorder(Border.NO_BORDER));
		note.addCell(detailCell(student.getGrade() + " Humanities Test 19 (Acer)").setFontSize(8f).setBold().setBorder(Border.NO_BORDER));
		return note;
	}

	// 2. title section
	private Table getTitleTable(float width, String message){ 
		Table note = new Table(new float[]{width});
		note.addCell(detailCell(message).setFontSize(8f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		return note;
	}

	// 3. answer section
	private Table getDetailAnswer(float width){
		Table body = new Table(new float[]{(width/2), (width/2)});
		Table left = getLeftDetailScore(width);
		Table right = getRightDetailScore(width);
		body.addCell(new Cell().add(left).setBorder(Border.NO_BORDER));
		body.addCell(new Cell().add(right).setBorder(Border.NO_BORDER));
		return body;
	}

	// 5. stats section
	private Table getStatisticsTable(float width){
		Table body = new Table(new float[]{(width/2), (width/2)});
		Image left = getBarChart(width);
		Image right = getBarChart(width);
		body.addCell(new Cell().add(left).setBorder(Border.NO_BORDER));
		body.addCell(new Cell().add(right).setBorder(Border.NO_BORDER));
		return body;
	}

	private Image getBarChart(float width) {
		// Create a chart and add it to the PDF
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(15, "", "Your Mark");
		dataset.addValue(52, "", "Average");
		dataset.addValue(83, "", "Highest");
		dataset.addValue(30, "", "Lowest");
		
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

		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDefaultItemLabelsVisible(true);
		renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
		renderer.setBarPainter(new StandardBarPainter());

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

	// left answer score section
	private Table getLeftDetailScore(float width){ 
		Table subject = new Table(new float[]{(width/2)});
		Table details = new Table(new float[]{(width/2)/10, ((width/2)/10)*2, (width/2)/10, ((width/2)/10)*2, ((width/2)/10)*4});
		details.addCell(detailCell("Q.No").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Resp").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Ans").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Percent").setBold().setBorder(Border.NO_BORDER)).setTextAlignment(TextAlignment.CENTER);
		details.addCell(detailCell("Topic").setBold().setBorder(Border.NO_BORDER));
		int count1 = 30;
		for(int i=0; i< count1; i++){
			// background color
			com.itextpdf.kernel.color.Color backgroundColor = (i % 2 == 0) ? com.itextpdf.kernel.color.Color.LIGHT_GRAY : com.itextpdf.kernel.color.Color.WHITE;
			Cell cell1 = detailCell((i + 1) + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setBold().setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell1);			
			Cell cell2 = detailCell("C - O").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell2);			
			Cell cell3 = detailCell("C").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell3);			
			Cell cell4 = detailCell("57").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER); // percent
			details.addCell(cell4);			
			Cell cell5 = detailCell("Comprehension").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.LEFT); // topics
			details.addCell(cell5);
		}
		subject.addCell(details.setMarginTop(1));
		return subject;
	}

	private Table getRightDetailScore(float width){ 
		Table subject = new Table(new float[]{(width/2)});
		Table details = new Table(new float[]{(width/2)/10, ((width/2)/10)*2, (width/2)/10, ((width/2)/10)*2, ((width/2)/10)*4});
		
		details.addCell(detailCell("Q.No").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Resp").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Ans").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Percent").setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
		details.addCell(detailCell("Topic").setBold().setBorder(Border.NO_BORDER));
		
		int count2 = 50;
		for(int i=30; i< count2; i++){
			// background color
			com.itextpdf.kernel.color.Color backgroundColor = (i % 2 == 0) ? com.itextpdf.kernel.color.Color.LIGHT_GRAY : com.itextpdf.kernel.color.Color.WHITE;
			Cell cell1 = detailCell((i + 1) + "").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setBold().setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell1);            
			Cell cell2 = detailCell("B - X").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell2);            
			Cell cell3 = detailCell("A").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER);
			details.addCell(cell3);            
			Cell cell4 = detailCell("31").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.CENTER); // percent
			details.addCell(cell4);            
			Cell cell5 = detailCell("Spelling").setBorder(Border.NO_BORDER).setBackgroundColor(backgroundColor).setTextAlignment(TextAlignment.LEFT); // topics
			details.addCell(cell5);
		}
		
		subject.addCell(details.setMarginTop(1).setBorder(Border.NO_BORDER));
		subject.setBorder(Border.NO_BORDER);
		return subject;
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

}
