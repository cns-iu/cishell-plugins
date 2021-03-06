package edu.iu.sci2.database.isi.match.reference_to_paper;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Dictionary;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.framework.data.BasicData;
import org.cishell.framework.data.Data;
import org.cishell.framework.data.DataProperty;
import org.cishell.service.database.Database;
import org.cishell.service.database.DatabaseCopyException;
import org.cishell.service.database.DatabaseService;
import org.cishell.utilities.DatabaseUtilities;
import org.osgi.service.log.LogService;

import edu.iu.nwb.shared.isiutil.database.ISI;
import edu.iu.sci2.database.scholarly.model.entity.Document;
import edu.iu.sci2.database.scholarly.model.entity.Reference;
import edu.iu.cns.database.load.framework.Schema;

public class MatchReferenceToPaperAlgorithm implements Algorithm {
	public static final String BASE_EXCEPTION_MESSAGE =
		"An error occurred when attempting to match references to papers: ";

	public static final String STRING_TEMPLATE_BASE_FILE_PATH =
		"/edu/iu/sci2/database/isi/match/reference_to_paper/stringtemplate/";
	public static final String STRING_TEMPLATE_FILE_PATH =
		STRING_TEMPLATE_BASE_FILE_PATH + "MatchReferenceToPaperQuery.st";

	public static final StringTemplateGroup matchReferencesToPapersGroup = loadTemplates();
	
	// Print debug messages?
	private static final boolean debug = false;
    private Data inData;
    private Database originalDatabase;
    private LogService logger;
    private DatabaseService databaseProvider;
    
    public MatchReferenceToPaperAlgorithm(
    		Data[] data, Dictionary parameters, CIShellContext ciShellContext) {
        this.inData = data[0];
        this.originalDatabase = (Database) this.inData.getData();

        this.logger = (LogService)ciShellContext.getService(LogService.class.getName());
        this.databaseProvider =
        	(DatabaseService)ciShellContext.getService(DatabaseService.class.getName());
    }

    public Data[] execute() throws AlgorithmExecutionException {
    	try {
    		Database newDatabase = this.databaseProvider.copyDatabase(this.originalDatabase);
    		Connection connection = DatabaseUtilities.connect(
    			newDatabase,
    			BASE_EXCEPTION_MESSAGE + "Unable to communicate with the selected database.");
    		Statement statement = DatabaseUtilities.createStatement(
    			connection,
    			BASE_EXCEPTION_MESSAGE + "Unable to interface with the selected database.");

    		try {
    			matchReferencesToPapers(statement);
    			reportOvermatchedReferences(statement);
    		} catch (SQLException e) {
    			String exceptionMessage = BASE_EXCEPTION_MESSAGE + e.getMessage();

    			throw new AlgorithmExecutionException(exceptionMessage, e);
    		} finally {
    			DatabaseUtilities.closeConnectionQuietly(connection);
    		}

    		Data outData = wrapForOutput(newDatabase);

    		return new Data[] { outData };
    	} catch (DatabaseCopyException e) {
    		String exceptionMessage =
    			"Failed to do automatic reference to paper matching because " +
    			"the input database could not be copied.\n" +
    			"Internal reason (useful for Help Desk requests): " + e.getMessage();
    		throw new AlgorithmExecutionException(exceptionMessage, e);
    	}
    }

    public static void logDebug(String debugMessage) {
    	if(!debug) {
    		return;
    	} 
    	
    	System.err.println(debugMessage);
    }
    
	private void reportOvermatchedReferences(Statement statement)
			throws SQLException {
		String query = createSelectOvermatchedReferencesQuery();
		logDebug("Overmatched Query:\n" + query);
		ResultSet overmatched = statement.executeQuery(query);
		boolean firstRow = true;
		while (overmatched.next()) {
			if (firstRow) {
				firstRow = false;
				this.logger
						.log(LogService.LOG_WARNING,
								"Some references matched more than one document."
										+ " This may occur because of corrections, reprints, or data errors (including duplication of records)"
										+ " None of these references will be pointed at documents."
										+ " The references with this problem are:");
			}
			this.logger.log(
					LogService.LOG_WARNING,
					"  " + overmatched.getString(1) + " ("
							+ overmatched.getInt(2) + " documents)");
		}
	}

	private static String createSelectOvermatchedReferencesQuery() {
		StringTemplate queryTemplate = matchReferencesToPapersGroup.getInstanceOf(
		"selectOvermatchedReferencesQuery");
		queryTemplate.setAttribute("referenceTableName", ISI.REFERENCE_TABLE_NAME);
		queryTemplate.setAttribute("referencePK", Schema.PRIMARY_KEY);
		queryTemplate.setAttribute("referenceRaw", Reference.Field.RAW_CITATION);
		queryTemplate.setAttribute("referenceAuthorFK", Reference.Field.AUTHOR_ID);
		queryTemplate.setAttribute("referencePageNumber", Reference.Field.PAGE_NUMBER);
		queryTemplate.setAttribute("referencePaperFK", Reference.Field.DOCUMENT_ID);
		queryTemplate.setAttribute("referenceSource", Reference.Field.SOURCE_ID);
		queryTemplate.setAttribute("referenceVolume", Reference.Field.VOLUME);
		queryTemplate.setAttribute("referenceYear", Reference.Field.YEAR);
		queryTemplate.setAttribute("documentTableName", ISI.DOCUMENT_TABLE_NAME);
		queryTemplate.setAttribute("documentPK", Schema.PRIMARY_KEY);
		queryTemplate.setAttribute("documentBeginningPage", Document.Field.BEGINNING_PAGE);
		queryTemplate.setAttribute("documentFirstAuthorFK", Document.Field.FIRST_AUTHOR_ID);
		queryTemplate.setAttribute("documentSource", Document.Field.SOURCE_ID);
		queryTemplate.setAttribute("documentVolume", Document.Field.VOLUME);
		queryTemplate.setAttribute("documentYear", Document.Field.PUBLICATION_YEAR);

	return queryTemplate.toString();
	}

	private void matchReferencesToPapers(Statement statement)
    		throws SQLException {
    	String query = createMatchReferencesToDocumentsQuery();
    	logDebug("Match References to Documents Query:\n" + query);
    	if (!statement.execute(query)) {
    		int updateCount = statement.getUpdateCount();

    		if (updateCount == 0) {
    			String logMessage =
    				"0 references matched with documents.\n" +
    				"The results of this algorithm directly depend on at least authors having " +
    				"been merged first, if not sources as well.\n" +
    				"To get better results, perform author and source merging before running " +
    				"this algorithm.";
    			this.logger.log(LogService.LOG_WARNING, logMessage);
    		} else {
    			String logMessage = updateCount + " references were matched with documents.";
    			this.logger.log(LogService.LOG_INFO, logMessage);
    		}
    	}
    }

    private static String createMatchReferencesToDocumentsQuery() {
    	StringTemplate queryTemplate = matchReferencesToPapersGroup.getInstanceOf(
    		"matchReferenceToPaperQuery");
    	queryTemplate.setAttribute("referenceTableName", ISI.REFERENCE_TABLE_NAME);
    	queryTemplate.setAttribute("referencePK", Schema.PRIMARY_KEY);
    	queryTemplate.setAttribute("referenceAuthorFK", Reference.Field.AUTHOR_ID);
    	queryTemplate.setAttribute("referencePageNumber", Reference.Field.PAGE_NUMBER);
    	queryTemplate.setAttribute("referencePaperFK", Reference.Field.DOCUMENT_ID);
    	queryTemplate.setAttribute("referenceSource", Reference.Field.SOURCE_ID);
    	queryTemplate.setAttribute("referenceVolume", Reference.Field.VOLUME);
    	queryTemplate.setAttribute("referenceYear", Reference.Field.YEAR);
    	queryTemplate.setAttribute("documentTableName", ISI.DOCUMENT_TABLE_NAME);
    	queryTemplate.setAttribute("documentPK", Schema.PRIMARY_KEY);
    	queryTemplate.setAttribute("documentBeginningPage", Document.Field.BEGINNING_PAGE);
    	queryTemplate.setAttribute("documentFirstAuthorFK", Document.Field.FIRST_AUTHOR_ID);
    	queryTemplate.setAttribute("documentSource", Document.Field.SOURCE_ID);
    	queryTemplate.setAttribute("documentVolume", Document.Field.VOLUME);
    	queryTemplate.setAttribute("documentYear", Document.Field.PUBLICATION_YEAR);

    	return queryTemplate.toString();
    }

    private Data wrapForOutput(Database database) {
    	Data outData = new BasicData(database, ISI.ISI_DATABASE_MIME_TYPE);
    	Dictionary<String, Object> parentMetadata = this.inData.getMetadata();
    	Dictionary<String, Object> metadata = outData.getMetadata();
    	metadata.put(DataProperty.PARENT, this.inData);
    	metadata.put(DataProperty.TYPE, parentMetadata.get(DataProperty.TYPE));
    	metadata.put(DataProperty.LABEL, "with references and papers matched");

    	return outData;
    }

    private static StringTemplateGroup loadTemplates() {
    	return new StringTemplateGroup(
    		new InputStreamReader(MatchReferenceToPaperAlgorithm.class.getResourceAsStream(
    			STRING_TEMPLATE_FILE_PATH)));
    }
}