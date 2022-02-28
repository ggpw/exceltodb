package com.exceltopostgre.ggpw.exceltopostgre.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CellData {
	private String columnName;
	private Integer columnOrder;
	private String columnType;
}
