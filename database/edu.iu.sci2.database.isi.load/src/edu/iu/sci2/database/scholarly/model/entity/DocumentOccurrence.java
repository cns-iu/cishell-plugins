package edu.iu.sci2.database.scholarly.model.entity;

import static edu.iu.sci2.database.scopus.load.EntityUtils.putPK;

import java.util.Dictionary;
import java.util.Hashtable;

import edu.iu.cns.database.load.framework.DBField;
import edu.iu.cns.database.load.framework.DerbyFieldType;
import edu.iu.cns.database.load.framework.RowItem;
import edu.iu.cns.database.load.framework.Schema;
import edu.iu.nwb.shared.isiutil.database.ISI;

public class DocumentOccurrence extends RowItem<DocumentOccurrence> {
	public static enum Field implements DBField {
		DOCUMENT_ID,
		FILE_ID;

		@Override
		public DerbyFieldType type() {
			return DerbyFieldType.FOREIGN_KEY;
		}
	}
	
	public static final Schema<DocumentOccurrence> SCHEMA = new Schema<DocumentOccurrence>(false, Field.values())
			.FOREIGN_KEYS(
					Field.DOCUMENT_ID.name(), ISI.DOCUMENT_TABLE_NAME,
					Field.FILE_ID.name(), ISI.ISI_FILE_TABLE_NAME);

	public DocumentOccurrence(Dictionary<String, Object> attributes) {
		super(attributes);
	}

	public static DocumentOccurrence link(
			Document document, ISIFile dataFile) {
		Dictionary<String, Object> attribs = new Hashtable<String, Object>();
		
		putPK(attribs, Field.DOCUMENT_ID, document);
		putPK(attribs, Field.FILE_ID, dataFile);
		
		return new DocumentOccurrence(attribs);
	}
}
