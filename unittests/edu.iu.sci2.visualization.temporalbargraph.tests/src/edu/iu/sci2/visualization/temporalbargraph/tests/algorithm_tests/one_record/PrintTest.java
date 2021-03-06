package edu.iu.sci2.visualization.temporalbargraph.tests.algorithm_tests.one_record;

import static edu.iu.sci2.visualization.temporalbargraph.tests.Utilities.testTBGAlgorithm;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.framework.algorithm.AlgorithmFactory;
import org.cishell.framework.data.BasicData;
import org.cishell.framework.data.Data;
import org.cishell.utilities.DateUtilities;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedMap;

import edu.iu.nwb.converter.prefusecsv.reader.PrefuseCsvReader;
import edu.iu.sci2.visualization.temporalbargraph.common.AbstractTemporalBarGraphAlgorithm;
import edu.iu.sci2.visualization.temporalbargraph.common.AbstractTemporalBarGraphAlgorithmFactory;
import edu.iu.sci2.visualization.temporalbargraph.print.TemporalBarGraphAlgorithmFactory;
import edu.iu.sci2.visualization.temporalbargraph.tests.TestDataLoadingUtilities;

public class PrintTest {
	private static ImmutableSortedMap<String, Object> commonParameters;
	private static Data[] convertedData;

	@BeforeClass
	public static void runBeforeClass() {
		try {
			File inFile = TestDataLoadingUtilities.getOneRecordData();
			Data data = new BasicData(inFile,
					AbstractTemporalBarGraphAlgorithm.CSV_MIME_TYPE);

			PrefuseCsvReader prefuseCSVReader = new PrefuseCsvReader(
					new Data[] { data });
			convertedData = prefuseCSVReader.execute();

			Map<String, Object> parameters = new Hashtable<String, Object>();
			parameters.put(
					AbstractTemporalBarGraphAlgorithmFactory.LABEL_FIELD_ID,
					"Name");
			parameters
					.put(AbstractTemporalBarGraphAlgorithmFactory.START_DATE_FIELD_ID,
							"Start");
			parameters.put(
					AbstractTemporalBarGraphAlgorithmFactory.END_DATE_FIELD_ID,
					"End");
			parameters.put(
					AbstractTemporalBarGraphAlgorithmFactory.SIZE_BY_FIELD_ID,
					"Amount");
			parameters
					.put(AbstractTemporalBarGraphAlgorithmFactory.DATE_FORMAT_FIELD_ID,
							DateUtilities.MONTH_DAY_YEAR_DATE_FORMAT);
			parameters.put(
					AbstractTemporalBarGraphAlgorithmFactory.CATEGORY_FIELD_ID,
					AbstractTemporalBarGraphAlgorithmFactory.DO_NOT_PROCESS_CATEGORY_VALUE);
			commonParameters = ImmutableSortedMap.copyOf(parameters);
		} catch (AlgorithmExecutionException e) {
			fail("The Prefuse CSV Reader failed: " + e.getMessage());
		}
	}

	@Test
	public void testPrintStandard() {
		Dictionary<String, Object> parameters = new Hashtable<String, Object>(
				PrintTest.commonParameters);
		parameters.put(TemporalBarGraphAlgorithmFactory.SUBTITLE_FIELD_ID,
				"Generated from test.");
		parameters
				.put(AbstractTemporalBarGraphAlgorithmFactory.SHOULD_SCALE_OUTPUT_FIELD_ID,
						false);
		parameters.put(TemporalBarGraphAlgorithmFactory.SIMPLIFIED_LAYOUT_ID, false);
		
		AlgorithmFactory algorithmFactory = new TemporalBarGraphAlgorithmFactory();
		try {
			testTBGAlgorithm(algorithmFactory, parameters,
					PrintTest.convertedData);
		} catch (AlgorithmExecutionException e) {
			e.printStackTrace();
			fail("Algorithm failed.");
		}
	}

	@Test
	public void testPrintScaled() {
		Dictionary<String, Object> parameters = new Hashtable<String, Object>(
				PrintTest.commonParameters);
		parameters.put(TemporalBarGraphAlgorithmFactory.SUBTITLE_FIELD_ID,
				"Generated from test.");
		parameters
				.put(AbstractTemporalBarGraphAlgorithmFactory.SHOULD_SCALE_OUTPUT_FIELD_ID,
						true);
		parameters.put(TemporalBarGraphAlgorithmFactory.SIMPLIFIED_LAYOUT_ID, false);
		
		AlgorithmFactory algorithmFactory = new TemporalBarGraphAlgorithmFactory();
		try {
			testTBGAlgorithm(algorithmFactory, parameters,
					PrintTest.convertedData);
		} catch (AlgorithmExecutionException e) {
			e.printStackTrace();
			fail("Algorithm failed.");
		}
	}
}
