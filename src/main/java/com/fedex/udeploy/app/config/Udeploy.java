package com.fedex.udeploy.app.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fedex.udeploy.app.domain.DataCenter;
import com.fedex.udeploy.app.domain.UDComponent;

import lombok.Getter;



@Getter
@Component
public class Udeploy {

	private static final String DATACENTER = "Data Center";
	private static final String LEVELS = "Levels";

	private String team;
	private String appName;
	private String appEAI;
	private String resourceGroup;
	private List<UDComponent> components;
	private List<DataCenter> dataCenters;

	@JsonIgnore
	private final DataFormatter dataFormatter;

	@Value("${input-file:}")
	private String inputFile;
	
	@Value("${app-index:1}")
	private Integer appIndex;
	
	@Value("${component-index:2}")
	private Integer comIndex;

	public Udeploy() {
		dataFormatter = new DataFormatter();
	}
	
	@PostConstruct
	public void buildConfig() throws EncryptedDocumentException, IOException {
		final Workbook workbook = WorkbookFactory.create(new File(inputFile));
		final Sheet appSheet = workbook.getSheetAt(appIndex -1);
		final int rowCount = appSheet.getPhysicalNumberOfRows();
		initSimpleFields(appSheet);
		buildDCGraph(appSheet, rowCount);

		try {
			final Sheet comSheet = workbook.getSheetAt(comIndex -1);
			buildComponentGraph(comSheet);
		}catch(Exception ex) {
			System.err.println("Component sheet is not available!");
		}
	}

	private void initSimpleFields(Sheet appSheet) {
		appName = dataFormatter.formatCellValue(appSheet.getRow(0).getCell(1));
		appEAI = dataFormatter.formatCellValue(appSheet.getRow(3).getCell(1));
		resourceGroup = dataFormatter.formatCellValue(appSheet.getRow(5).getCell(1));
		team = dataFormatter.formatCellValue(appSheet.getRow(7).getCell(1));
		dataCenters = new ArrayList<>();
		components = new ArrayList<>();
	}

	private void buildComponentGraph(Sheet componentSheet) {
		for(int i = 1; i<=componentSheet.getLastRowNum(); i++) {
			Row row = componentSheet.getRow(i);
			if(isRowEmpty(row)) break;
			components.add(new UDComponent() {{
				setComponentName(dataFormatter.formatCellValue(row.getCell(0)));
				setComponentDesc(dataFormatter.formatCellValue(row.getCell(1)));
				setComponentDiskSpace(dataFormatter.formatCellValue(row.getCell(2)));
				setComponentSudoUser(dataFormatter.formatCellValue(row.getCell(3)));
				setComponentPath(dataFormatter.formatCellValue(row.getCell(4)));
				setComponentDeployDir(dataFormatter.formatCellValue(row.getCell(5)));
				setComponentAppName(dataFormatter.formatCellValue(row.getCell(6)));
			}});
		}
	}

	private void buildDCGraph(Sheet appSheet, int rowCount) {
		List<Integer> dcIndexes = findDCIndexes(appSheet, rowCount);
		if (!dcIndexes.isEmpty()) {
			dataCenters = new ArrayList<>(dcIndexes.size());
		}
		for (int i = 0; i < dcIndexes.size(); i++) {
			int cdci = dcIndexes.get(i);
			int ndci = (i + 1) < dcIndexes.size() ? dcIndexes.get(i + 1) : rowCount;
			final String dcName = dataFormatter.formatCellValue(appSheet.getRow(cdci).getCell(1)).trim();
			final String compName = dataFormatter.formatCellValue(appSheet.getRow(cdci - 2).getCell(1)).trim();
			DataCenter dc = new DataCenter(dcName, compName);
			List<String> levelNames = findLevels(dc, appSheet, cdci);
			addAgentsToDCLevels(appSheet, levelNames, dc, cdci, ndci);
			dataCenters.add(dc);
		}
	}

	private List<Integer> findDCIndexes(Sheet appSheet, int rowCount) {
		int dcIndex = 0;
		List<Integer> dcIndexes = new ArrayList<>();
		while (dcIndex < rowCount && dcIndex >= 0) {
			dcIndex = findDCIndex(dcIndex + 1, rowCount, appSheet, rowCount);
			if (dcIndex > 0) {
				System.out.println("FOUND DC in ROW : " + (dcIndex + 1));
				dcIndexes.add(dcIndex);
			}
		}
		return dcIndexes;
	}

	public List<String> findLevels(DataCenter dc, Sheet appSheet, int cdci) {
		List<String> levelNames = new ArrayList<>();
		for (Cell level : appSheet.getRow(cdci + 1)) {
			String levelName = dataFormatter.formatCellValue(level).trim();
			if (!LEVELS.equalsIgnoreCase(levelName) && levelName.length() > 0) {
				dc.addLevel(levelName);
				levelNames.add(levelName);
			}
		}
		return levelNames;
	}

	private void addAgentsToDCLevels(Sheet appSheet, List<String> levelNames, DataCenter dc, int cdci, int ndci) {
		for (int j = cdci + 2; j < ndci; j++) {
			Row row = appSheet.getRow(j);
			if (row != null) {
				for (int k = 1; k <= levelNames.size(); k++) {
					String agentName = dataFormatter.formatCellValue(row.getCell(k)).trim();
					if (agentName.length() > 0) {
						dc.addAgent(levelNames.get(k - 1), agentName);
					}
				}
			}
		}
	}

	private int findDCIndex(int fromRow, int toRow, Sheet appShheet, int rowCount) {
		for (int i = fromRow; i < rowCount; i++) {
			Row row = appShheet.getRow(i);
			if (row != null && !isRowEmpty(row)) {
				for (Cell cell : row) {
					if (DATACENTER.equalsIgnoreCase(dataFormatter.formatCellValue(cell).trim())) {
						return row.getRowNum();
					}
				}
			}
		}
		return -1;
	}

	private boolean isRowEmpty(Row row) {
		if (row != null) {
			for (Cell cell : row) {
				if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
					return false;
				}
			}
		}
		return true;
	}
}
