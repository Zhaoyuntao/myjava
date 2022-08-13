import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ImportExecl {

	static String excelPath = "/Users/a1234/Desktop/languages/";

	static String xmlPath = "/Users/a1234/Desktop/languages/";

	static String excelPath2 = "/Users/a1234/Desktop/俄语.xlsx";
	static String xmlPath2 = "/Users/a1234/Desktop/strings_totok2.xml";

	static final String IOS = "iOS";
	static final String ANDROID = "android";

	public static class Item {
		String key = "";
		String value = "";
		boolean okKey;
		boolean okValue;
	}

	public static void convert(String type, String filename, int sheetIndex, final int keyIndex, final int valueIndex, int maxShowLine, int maxShowErrorLine) {
		s("--------------------------------------------------------------------------------------------------------------------------------");
		final int keyIndex2 = keyIndex;
		s("" + filename);
		String filePath = null;
		String fileOutputPath = null;
		if (ANDROID.equals(type)) {
			filePath = excelPath;
			fileOutputPath = xmlPath;
		} else {
			filePath = excelPath2;
			fileOutputPath = xmlPath2;
		}
		// String encoding = "GBK";
		File excel = new File(filePath + filename);
		File fileOutput = new File(fileOutputPath + "output_" + filename + ".xml");
		if (excel.isFile() && excel.exists()) { // 判断文件是否存在

			String[] split = excel.getName().split("\\."); // .是特殊字符，需要转义！！！！！
			Workbook wb = null;
			// 根据文件后缀（xls/xlsx）进行判断
			if ("xls".equals(split[1])) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(excel);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // 文件流对象
				try {
					wb = new HSSFWorkbook(fis);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("xlsx".equals(split[1])) {
				try {
					wb = new XSSFWorkbook(excel);
				} catch (InvalidFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				e("文件类型错误!");
				return;
			}

			// 开始解析
			Sheet sheet = wb.getSheetAt(sheetIndex); // 读取sheet
			s("Sheet[" + sheetIndex + "]:" + sheet.getSheetName());
			int firstRowIndex = sheet.getFirstRowNum();
			int lastRowIndex = sheet.getLastRowNum();
			s("第一行：" + firstRowIndex + " 最后一行：" + lastRowIndex);
			List<Item> list = new ArrayList<>();
			List<Item> error = new ArrayList<>();
			int showLineCount = 0;
			s("-----------------------------------------------");
			row: for (int i = firstRowIndex; i <= lastRowIndex; i++) { // 遍历行
				Row row = sheet.getRow(i);
				Item item = new Item();
				if (row == null) {
					item.okValue = false;
					item.okKey = false;
					item.key = "empty_" + i;
					continue row;
				}
				int firstCellIndex = row.getFirstCellNum();
				int lastCellIndex = row.getLastCellNum();

				int index = 0;
				for (int j = firstCellIndex; j < lastCellIndex; j++) { // 遍历列
					Cell cell = row.getCell(j);
					String cellContent = null;
					if (cell != null && cell.getCellType() != CellType.NUMERIC) {

						try {
							cellContent = cell.getStringCellValue();
						} catch (Exception e) {
							item.okKey = false;
							continue;
						}
//							s(cellContent);
						// 跳过第一个题注
						if (cellContent.contains("题注")) {
							e("题注:" + cellContent);
							item.okValue = false;
							item.okKey = false;
							item.key = "题注_" + i;
							continue row;
						}
						if (index == keyIndex) {
							if (cellContent == null || Pattern.matches("[ \f\n\r\t]*", cellContent)) {
								item.okKey = false;
							} else if (!Pattern.compile("[a-z_A-Z0-9]*").matcher(cellContent).matches()) {
								item.key = cellContent.trim();
								item.okKey = false;
							} else {
								item.key = cellContent.trim();
								item.okKey = true;
							}
						} else if (index == valueIndex) {
							item.value = cellContent.trim();
							item.okValue = true;
//									if (valueArabic.endsWith("...")) {
//										valueArabic = "..." + valueArabic.substring(0, valueArabic.length() - 3);
//									} else if (valueArabic.endsWith(".")) {
//										valueArabic = "." + valueArabic.substring(0, valueArabic.length() - 1);
//									} else if (valueArabic.endsWith("?")) {
//										valueArabic = "?" + valueArabic.substring(0, valueArabic.length() - 1);
//									} else if (valueArabic.endsWith("!")) {
//										valueArabic = "!" + valueArabic.substring(0, valueArabic.length() - 1);
//									}
						} else {
							continue;
						}
						index++;
					}
				}
				if (++showLineCount <= maxShowLine) {
					if (item.okValue) {
						s(item.value);
					} else {
						e(item.value);
					}
				}

				if (item.okKey && item.okValue) {
					list.add(item);
				} else {
					error.add(item);
				}
			}

			String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<resources>\n";
			String tail = "</resources>";
			StringBuilder sb = new StringBuilder();
			if (ANDROID.equals(type)) {
				sb.insert(0, head);
				// normal
				for (int i = 0; i < list.size(); i++) {
					Item item = list.get(i);
					if (ANDROID.equals(type)) {
						sb.append("	<string name=\"" + item.key + "\">" + item.value + "</string>\n");
					} else {
						sb.append("<key>" + item.key + "</key>\n<string>" + item.value + "</string>\n\n");
					}
				}
				sb.append("\n\n\n\n\n\n\n\n<!-- ===================================[all:" + (list.size() + error.size()) + ",correct:" + list.size() + ",error:" + error.size() + "]=================================== -->\n\n\n\n\n\n\n\n");
				// error
				for (int i = 0; i < error.size(); i++) {
					Item item = error.get(i);
					if (ANDROID.equals(type)) {
						sb.append("	<string name=\"" + item.key + "\">" + item.value + "</string>\n");
					} else {
						sb.append("<key>" + item.key + "</key>\n<string>" + item.value + "</string>\n\n");
					}
				}

				sb.append(tail);
			}

			if (fileOutput.exists()) {
				fileOutput.delete();
			}
			OutputStreamWriter fileOutputStream;
			try {
				fileOutputStream = new OutputStreamWriter(new FileOutputStream(fileOutput));
				fileOutputStream.write(sb.toString());
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			s("可用条目：" + list.size());
			e("错误/缺失条目(：" + error.size() + ")----------------------------------------------------");
			for (int i = 0; i < error.size() && i < maxShowErrorLine; i++) {
				Item item = error.get(i);
				e("错误条目：[" + item.key + "] [" + item.value + "]");
			}
			e("错误/缺失条目(：" + error.size() + ")----------------------------------------------------");
		} else {
			System.out.println("找不到指定的文件");
		}

	}

	public static void s(Object o) {
		System.out.println(o);
	}

	public static void e(Object o) {
		System.err.println(o);
	}

	public static void main(String[] args) {
		String type = ANDROID;
		int sheet = 0;
		int keyIndex = 0;
		int valueIndex = 1;
		int maxShowLine = 3;
		int maxShowErrorLine = 3;
		String[] files = { "italian", "indonesian_yinni", "hindi", "hebrew_xibolai", "german", "french", "amharic_aisaiebiya" };
		String[] files2 = { "malayalam", "persian", "portuguese", "spanish", "turkish", "urdu" };
		for (String filename : files) {

			convert(type, filename + ".xlsx", sheet, keyIndex, valueIndex, maxShowLine, maxShowErrorLine);
		}

	}

}
