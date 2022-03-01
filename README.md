# exceltodb
Simple Java Application to Load Excel Data to any DB

## Prequisites
java 11 and maven already been installed

## About
This is a simple java application based on spring-boot to help you migrating fresh data to empty tables.
As for this version it only support single schema. But it already support multiple table. And only xlsx file.

### 1. Setup your datasource
Setup your datasource in application.yml. For this version it only support one datasource

### 2. Setup your json mapping
Take a look on folder src/main/resources/jsonschema.json. This json file acts as mapper from your excel files to your tables

```
  {
	"datas": [
		{
			"fileName": "Dummy",
			"tableName": "dummy",
			"isBooleanSupport": true,
			"fields": [
				{
					"columnName": "id",
					"columnOrder": -1,
					"columnType": "integer",
					"isIncrement": true
				},
				{
					"columnName": "version",
					"columnOrder": -1,
					"columnType": "integer",
					"default": 0
				},
				{
					"columnName": "first_name",
					"columnOrder": 0,
					"columnType": "string"
				}
			]
		}
	]
}
```
#### Explanation
<ul>
	<li>filename : your excel file name. Be sure that your excel format is xlsx</li>
	<li>tableName: table name that you want to insert the data</li>
	<li>isBooleanSupport: please refer to this [link] (https://www.databasestar.com/sql-boolean-data-type/)</li>
	<li>fields : all your fields that you want to insert, there might be case that there are column in table that non available in excel but the column is mandatory</li>
	<ul>
		<li>columnName: column name in table</li>
		<li>columnOrder: the order of the column in excel, start with 0. -1 means it's not exist in excel. so you need to define the default value</li>
		<li>default: default value to fill in if the column is not mapped in excel</li>	
	</ul>
</ul>

### 3. Copy your XLSX file to folder resources
Copy your excel file to folder src/main/resources based on what mapped in jsonschema.json The exel file must be in format xlsx. The application will start on row number 2, row number 1 is ignored (column header).

### 4. Run the application
execute <code>mvn clean install</code> then <code>mvn spring-boot:run</code>

### 5. Wait for the result
Wait for the application to finish the execution. You might see some logs info or error. After the application is executed checked your desired table
