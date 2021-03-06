package edu.iu.sci2.database.star.common.utility.csv.validator;

import java.io.File;
import java.io.IOException;

import org.cishell.framework.data.Data;
import org.cishell.utilities.StringUtilities;

import au.com.bytecode.opencsv.CSVReader;
import edu.iu.sci2.database.star.common.utility.CSVReaderUtilities;
import edu.iu.sci2.database.star.common.utility.csv.validator.exception.CSVBodyValidationException;
import edu.iu.sci2.database.star.common.utility.csv.validator.exception.CSVHeaderValidationException;

/** This class is designed to provide a general way to validate CSV files.
 * It could probably benefit other code that reads CSVs, but only have further
 * testing and verification.
 * The basic idea is, the CSVDataValidator is general, but takes a specific CSVDataValidationRules
 * that validates certain things (the header and each row as its read). 
 */
public class CSVDataValidator {
	private CSVReader reader;
	private CSVDataValidationRules rules;
	private boolean hasValidatedHeader = false;

	public CSVDataValidator(Data data, CSVDataValidationRules rules) throws IOException {
		this((File) data.getData(), rules);
	}

	public CSVDataValidator(File file, CSVDataValidationRules rules) throws IOException {
		this.reader = CSVReaderUtilities.createCSVReader(file, true);
		this.rules = rules;
	}

	public void validateHeader()
			throws CSVHeaderValidationException {
		if (this.hasValidatedHeader) {
			String exceptionMessage =
				"(Developer Error) The header has already been validated.  " +
				"It may not be validated again with the same CSVDataValidator.";
			throw new CSVHeaderValidationException(exceptionMessage);
		}

		try {
			String[] header = StringUtilities.simpleCleanStrings(this.reader.readNext());
			this.rules.validateHeader(header);
			this.hasValidatedHeader = true;
		} catch (IOException e) {
			String exceptionMessage = String.format(
				"An error occurred when attempting to read the header: \"%s\"", e.getMessage());
			throw new CSVHeaderValidationException(exceptionMessage, e);
		}
	}

	public void validateBody()
			throws CSVBodyValidationException {
		if (!hasValidatedHeader) {
			try {
				validateHeader();
			} catch (CSVHeaderValidationException e) {
				String exceptionMessage =
					"(Developer Error) The header was not validated before calling " +
					"validateBody(), so validateBody() was called.  The following error " +
					"occurred when doing so: \"" + e.getMessage() + "\"";
				throw new CSVBodyValidationException(exceptionMessage, e);
			}
		}

		try {
			String[] row;

			while ((row = this.reader.readNext()) != null) {
				this.rules.validateRow(row);
			}
		} catch (IOException e) {
			String exceptionMessage = String.format(
				"An error occurred when attempting to read the next row: \"%s\"", e.getMessage());
			throw new CSVBodyValidationException(exceptionMessage, e);
		}
	}
}