package com.ssangyong.common.utils;

import java.io.File;
import java.util.Vector;

import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class DatasetService {
	private static DatasetService service;
	private static TCComponentDatasetType datasetType;

	public static void createService(TCSession session) {
		if (service == null) {
			service = new DatasetService(session);
		}
	}

	private DatasetService(TCSession session) {
		try {
			datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public static TCComponentDataset createDataset(String path) throws Exception {
		TCComponentDataset dataset = null;
		File file = new File(path);
		if (file != null) {
			String extension = getExtension(file);
			if (extension != null && !extension.equals("")) {
				if (extension.equals("xls")) {
					dataset = datasetType.create(getFileName(file), "", "MSExcel");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcel" },
							new String[] { "Plain" }, new String[] { "excel" });
				} else if (extension.equals("xlsx")) {
					dataset = datasetType.create(getFileName(file), "", "MSExcelX");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcelX" },
							new String[] { "Plain" }, new String[] { "excel" });
				} else if (extension.equals("doc")) {
					dataset = datasetType.create(getFileName(file), "", "MSWord");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSWord" },
							new String[] { "Plain" }, new String[] { "word" });
				} else if (extension.equals("docx")) {
					dataset = datasetType.create(getFileName(file), "", "MSWordX");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSWordX" },
							new String[] { "Plain" }, new String[] { "word" });
				} else if (extension.equals("ppt")) {
					dataset = datasetType.create(getFileName(file), "", "MSPowerPoint");
					dataset.setFiles(new String[] { file.getAbsolutePath() },
							new String[] { "MSPowerPoint" }, new String[] { "Plain" },
							new String[] { "powerpoint" });
				} else if (extension.equals("pptx")) {
					dataset = datasetType.create(getFileName(file), "", "MSPowerPointX");
					dataset.setFiles(new String[] { file.getAbsolutePath() },
							new String[] { "MSPowerPointX" }, new String[] { "Plain" },
							new String[] { "powerpoint" });
				} else if (extension.equals("txt")) {
					dataset = datasetType.create(getFileName(file), "", "Text");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Text" },
							new String[] { "Plain" }, new String[] { "Text" });
				} else if (extension.equals("pdf")) {
					dataset = datasetType.create(getFileName(file), "", "PDF");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "PDF" },
							new String[] { "Plain" }, new String[] { "PDF_Reference" });
				} else if (extension.equals("jpg")) {
					dataset = datasetType.create(getFileName(file), "", "JPEG");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "JPEG" },
							new String[] { "Plain" }, new String[] { "JPEG_Reference" });
				} else if (extension.equals("gif")) {
					dataset = datasetType.create(getFileName(file), "", "GIF");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "GIF" },
							new String[] { "Plain" }, new String[] { "GIF_Reference" });
				} else if (extension.equals("jpeg") || extension.equals("png") || extension.equals("tif")
						|| extension.equals("tiff") || extension.equals("bmp")) {
					dataset = datasetType.create(getFileName(file), "", "Image");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Image" },
							new String[] { "Plain" }, new String[] { "Image" });
				} else if (extension.equals("dwg")) {
					dataset = datasetType.create(getFileName(file), "", "ACADDWG");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "ACADDWG" },
							new String[] { "Plain" }, new String[] { "DWG" });
				} else if (extension.equals("zip")) {
					dataset = datasetType.create(getFileName(file), "", "Zip");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Zip" },
							new String[] { "Plain" }, new String[] { "ZIPFILE" });
				} else if (extension.equals("htm") || extension.equals("html")) {
					dataset = datasetType.create(getFileName(file), "", "HTML");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Text" },
							new String[] { "Plain" }, new String[] { "HTML" });
				} else if (extension.equals("eml")) {
					dataset = datasetType.create(getFileName(file), "", "EML");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "EML" },
							new String[] { "Plain" }, new String[] { "EML_Reference" });
				} else {
					dataset = datasetType.create(getFileName(file), "", "MISC");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MISC" },
							new String[] { "Plain" }, new String[] { "MISC_BINARY" });
				}
			}
		}
		return dataset;
	}

	public static void createDataset(TCComponent component, String relation, Vector<String> vector)
			throws Exception {
		for (int i = 0; i < vector.size(); i++) {
			TCComponentDataset dataset = createDataset(vector.elementAt(i));
			component.add(relation, dataset);
		}
	}

	public static TCComponentDataset[] createDataset(Vector<String> vector) throws Exception {
		TCComponentDataset[] datasets = new TCComponentDataset[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			TCComponentDataset dataset = createDataset(vector.elementAt(i));
			datasets[i] = dataset;
		}
		return datasets;
	}

	public static void createDataset(TCComponent component, String relation, String path) throws Exception {
		TCComponentDataset dataset = createDataset(path);
		if (dataset != null) {
			component.add(relation, dataset);
		}
	}

	public static File[] getFiles(TCComponentDataset dataset) throws Exception {
		String type = dataset.getType();
		String namedRefType = null;
		if (type.equals("MSExcel") || type.equals("MSExcelX")) {
			namedRefType = new String("excel");
		} else if (type.equals("MSWord") || type.equals("MSWordX")) {
			namedRefType = new String("word");
		} else if (type.equals("MSPowerPoint") || type.equals("MSPowerPointX")) {
			namedRefType = new String("powerpoint");
		} else if (type.equals("Text")) {
			namedRefType = new String("Text");
		} else if (type.equals("PDF")) {
			namedRefType = new String("PDF_Reference");
		} else if (type.equals("Image")) {
			namedRefType = new String("Image");
		} else if (type.equals("ACADDWG")) {
			namedRefType = new String("DWG");
		} else if (type.equals("Zip")) {
			namedRefType = new String("ZIPFILE");
		} else if (type.equals("HTML")) {
			namedRefType = new String("HTML");
		} else if (type.equals("MISC")) {
			namedRefType = new String("MISC_TEXT");
		}
		Registry client_specific = Registry.getRegistry("client_specific");
		String exportDir = client_specific.getString("TCExportDir");
		File folder = new File(exportDir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		File[] files = dataset.getFiles(namedRefType, exportDir);
		return files;
	}

	private static String getFileName(File file) throws Exception {
		if (file.isDirectory())
			return file.getName();
		else {
			String filename = file.getName();
			int i = filename.lastIndexOf(".");
			if (i > 0) {
				return filename.substring(0, i);
			}
		}
		return null;
	}

	private static String getExtension(File file) throws Exception {
		if (file.isDirectory())
			return null;
		String filename = file.getName();
		int i = filename.lastIndexOf(".");
		if (i > 0 && i < filename.length() - 1) {
			return filename.substring(i + 1).toLowerCase();
		}
		return null;
	}

	public static void datasetUpdate(File file, TCComponentDataset dataset) throws Exception {
		NamedReferenceContext[] namedRefContext = dataset.getDatasetDefinitionComponent()
				.getNamedReferenceContexts();
		for (int i = 0; i < namedRefContext.length; i++) {
			dataset.removeNamedReference(namedRefContext[i].getNamedReference());
		}
		TCComponentDatasetDefinition datasetDefinition = dataset.getDatasetDefinitionComponent();
		NamedReferenceContext namedRefTypes[] = datasetDefinition.getNamedReferenceContexts();
		String as1[] = { file.getAbsolutePath() };
		String as2[] = { namedRefTypes[0].getFileFormat() };
		String as3[] = { namedRefTypes[0].getMimeType() };
		String as4[] = { namedRefTypes[0].getNamedReference() };
		dataset.setFiles(as1, as2, as3, as4);
	}
}
