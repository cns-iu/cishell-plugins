package edu.iu.sci2.database.scholarly.model.entity;

import static edu.iu.sci2.database.scopus.load.EntityUtils.putPK;
import static edu.iu.sci2.database.scopus.load.EntityUtils.putValue;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import edu.iu.cns.database.load.framework.DBField;
import edu.iu.cns.database.load.framework.DerbyFieldType;
import edu.iu.cns.database.load.framework.RowItem;
import edu.iu.cns.database.load.framework.Schema;
import edu.iu.nwb.shared.isiutil.database.ISI;

public class Editor extends RowItem<Editor> {
	public static enum Field implements DBField {
		DOCUMENT_ID(DerbyFieldType.FOREIGN_KEY),
		PERSON_ID(DerbyFieldType.FOREIGN_KEY),
		ORDER_LISTED(DerbyFieldType.INTEGER);

		private final DerbyFieldType fieldType;
		private Field(DerbyFieldType type) {
			this.fieldType = type;
		}
		@Override
		public DerbyFieldType type() {
			return this.fieldType;
		}
	}
	
	public static final Schema<Editor> SCHEMA = new Schema<Editor>(
			false,
			Field.values()).
			FOREIGN_KEYS(
					Field.DOCUMENT_ID.name(), ISI.DOCUMENT_TABLE_NAME,
					Field.PERSON_ID.name(), ISI.PERSON_TABLE_NAME);
	
	public Editor(Dictionary<String, Object> attributes) {
		super(attributes);
	}

	public static List<Editor> makeEditors(Document document,
			List<Person> people) {
		List<Editor> editors = new ArrayList<Editor>();
		int sequence = 1;
		
		for (Person p : people) {
			Dictionary<String, Object> attribs = new Hashtable<String, Object>();
			putPK(attribs, Field.DOCUMENT_ID, document);
			putPK(attribs, Field.PERSON_ID, p);
			putValue(attribs, Field.ORDER_LISTED, sequence++);
			
			editors.add(new Editor(attribs));
		}
		
		return editors;
	}
}
