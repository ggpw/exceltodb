package com.exceltopostgre.ggpw.exceltopostgre.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelTable {
	private String fileName;
	private String tableName;
	private List<CellData> fields;
}
