package edu.iu.sci2.visualization.temporalbargraph.print;

import static edu.iu.sci2.visualization.temporalbargraph.utilities.PostScriptFormationUtilities.inchToPoint;

import java.util.List;

import org.cishell.utilities.color.ColorRegistry;

import au.com.bytecode.opencsv.CSVWriter;
import edu.iu.sci2.visualization.temporalbargraph.common.AbstractPages;
import edu.iu.sci2.visualization.temporalbargraph.common.DoubleDimension;
import edu.iu.sci2.visualization.temporalbargraph.common.Record;

public class PostscriptDocument
		extends
		edu.iu.sci2.visualization.temporalbargraph.common.AbstractPostscriptDocument {

	private DoubleDimension postscriptPageSize;
	private AbstractPages temporalBarGraphPages;

	public PostscriptDocument(CSVWriter csvWriter, List<Record> records,
			boolean scaleToOnePage, String areaColumn,
			String categoryColumn, ColorRegistry<String> colorRegistry, String labelColumn, String query,
			DoubleDimension pageSize) {

		this.postscriptPageSize = new DoubleDimension(inchToPoint(pageSize.getWidth()), inchToPoint(pageSize.getHeight()));
		
		if (this.postscriptPageSize.getWidth() > this.postscriptPageSize.getHeight()){
		this.temporalBarGraphPages = new TemporalBarGraphLandscapePages(csvWriter,
				records, scaleToOnePage, colorRegistry, this.postscriptPageSize,
				areaColumn, categoryColumn, labelColumn, query);
		} else {
			this.temporalBarGraphPages = new TemporalBarGraphPortraitPages(csvWriter,
					records, scaleToOnePage, colorRegistry, this.postscriptPageSize,
					areaColumn, categoryColumn, query);
		}
	}

	@Override
	protected AbstractPages getPages() {
		return this.temporalBarGraphPages;
	}

	@Override
	protected DoubleDimension getPageSize() {
		return this.postscriptPageSize;
	}

}