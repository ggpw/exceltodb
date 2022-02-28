package com.exceltopostgre.ggpw.exceltopostgre;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;

import com.exceltopostgre.ggpw.exceltopostgre.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Log4j2
public class ExceltopostgreApplication implements CommandLineRunner {

	@Autowired
	JdbcTemplate jdbcTemplate;
	ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) {
		SpringApplication.run(ExceltopostgreApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Integer s = jdbcTemplate.query("select 100", (rs, rowNum) -> {
			return rs.getInt(1);
		}).get(0);
		log.info("jdbc is connected: {}", s > 0);
//		readJson("jsonschema.json");
//		readExcel("demo.xlsx");
		process();
	}

	private void process() {
		JSONArray datas = readJson("jsonschema.json");
		StringBuilder queries = new StringBuilder();
		if (Objects.nonNull(datas)) {
			Iterator<JSONObject> it = datas.iterator();
			while (it.hasNext()) {
				JSONObject table = (JSONObject) it.next();
				queries.append(buildQueryFromExcel(table));
			}
		}
		String sqlScript = queries.toString();
		if (!StringUtils.isBlank(sqlScript)) {
			log.info("inserting to database ");
			jdbcTemplate.update(sqlScript);
		}
	}

	private JSONArray readJson(String fileName) {
		JSONParser parser = new JSONParser();
		JSONArray datas = null;
		try {
			File file = ResourceUtils.getFile("classpath:" + fileName);
			Object obj = parser.parse(new FileReader(file));
			JSONObject jsonObject = (JSONObject) obj;
			datas = (JSONArray) jsonObject.get("datas");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datas;
	}

	private String buildQueryFromExcel(JSONObject table) {
		String fileName = (String) table.get("fileName") + ".xlsx";
		String tableName = (String) table.get("tableName");
		Boolean isBooleanSupport = (Boolean) table.get("isBooleanSupport");
		StringBuilder query = new StringBuilder();
		log.info("Reading excel file {} to map table {}", fileName, tableName);
		try {
			JSONArray columns = (JSONArray) table.get("fields");
			File file = ResourceUtils.getFile("classpath:" + fileName);

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			Double incr = 1d;
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				log.info("iterating at row: {}", row.getRowNum());
				// Skip first row
				if (row.getRowNum() < 1) {
					continue;
				}
				StringBuilder insertInto = new StringBuilder();
				StringBuilder values = new StringBuilder();

				insertInto.append(String.format("INSERT INTO %s%s", tableName.toUpperCase(), "("));
				values.append("VALUES(");

				Iterator<JSONObject> columnIterator = columns.iterator();
				while (columnIterator.hasNext()) {
					Long columnOrder = 0l;
					try {
						JSONObject field = columnIterator.next();
						String columnName = (String) field.get("columnName");
						columnOrder = (Long) field.get("columnOrder");
						String columnType = (String) field.get("columnType");

						insertInto.append(String.format("%s,", columnName.toUpperCase()));

						if (columnOrder < 0) {
							incr = buildValuesNonMapped(columnType, field, values, incr);
							continue;
						}
						buildValuesMapped(row, columnOrder, columnType, values, isBooleanSupport);
					} catch (Exception e) {
						log.error("error while mapping cell({},{}) ", row.getRowNum(), columnOrder);
						log.error("error message: {}", e);
					}
				}
				insertInto.deleteCharAt(insertInto.length() - 1);
				insertInto.append(")");
				values.deleteCharAt(values.length() - 1);
				values.append(");");
				query.append(String.format("%s %s %n", insertInto.toString(), values.toString()));
				workbook.close();
			}
		} catch (Exception e) {
			log.error("error when building query {}", e.getMessage());
			log.error("error message: {}", e);
		}
		log.info("Query: \n{}", query.toString());
		return query.toString();
	}

	private Double buildValuesNonMapped(String columnType, JSONObject field, StringBuilder values, Double incr) {
		switch (columnType.toLowerCase()) {
		case "integer":
			if (Objects.nonNull(field.get(Constants.DEFAULT))) {
				Long def = (Long) field.get(Constants.DEFAULT);
				values.append(String.format("%d,", def));
			} else {
				// now just do increment
				values.append(String.format("%d,", incr.intValue()));
				incr++;
			}
			break;
		case "timestamp":
			if (Objects.nonNull(field.get(Constants.DEFAULT))) {
				String val = (String) field.get(Constants.DEFAULT);
				values.append(String.format("%s,", val));
			} else {
				values.append("CURRENT_TIMESTAMP(),");
			}
			break;
		default:
			break;
		}
		return incr;
	}

	private void buildValuesMapped(Row row, Long columnOrder, String columnType, StringBuilder values, boolean isBooleanSupport) {
		Cell cell = row.getCell(columnOrder.intValue());
		switch (cell.getCellType()) {
		case NUMERIC:
			Double val = cell.getNumericCellValue();
			if (StringUtils.containsAnyIgnoreCase(columnType, "SMALLINT", "INT", "SERIAL")) {
				values.append(String.format("%d,", val.intValue()));
			} else if (StringUtils.equalsIgnoreCase(columnType, "boolean")) {
				if(isBooleanSupport) {
					values.append(String.format("%b,", val > 0d));
				}else {
					values.append(String.format("%d,", val > 0d ? 1 : 0));
				}
			} else {
				values.append(String.format("%f,", val));
			}
			break;
		case STRING:
			values.append(String.format("'%s',", cell.getStringCellValue()));
			break;
		case BOOLEAN:
			if(isBooleanSupport) {
				values.append(String.format("%b,", cell.getBooleanCellValue()));
			}else {
				values.append(String.format("%d,", cell.getBooleanCellValue() ? 1 : 0));
			}
			break;
		}
	}
}
