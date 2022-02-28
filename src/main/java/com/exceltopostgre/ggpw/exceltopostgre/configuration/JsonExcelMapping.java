package com.exceltopostgre.ggpw.exceltopostgre.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.exceltopostgre.ggpw.exceltopostgre.model.dto.ExcelTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@PropertySource(value = "classpath:jsonschema.json", factory = JsonPropertySourceFactory.class)
@ConfigurationProperties
public class JsonExcelMapping {
	private List<ExcelTable> datas;
}
