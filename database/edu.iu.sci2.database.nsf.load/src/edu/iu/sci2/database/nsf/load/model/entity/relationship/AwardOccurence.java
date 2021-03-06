package edu.iu.sci2.database.nsf.load.model.entity.relationship;

import java.util.Dictionary;
import java.util.Hashtable;

import edu.iu.cns.database.load.framework.DerbyFieldType;
import edu.iu.cns.database.load.framework.RowItem;
import edu.iu.cns.database.load.framework.Schema;
import edu.iu.sci2.database.nsf.load.model.entity.Award;
import edu.iu.sci2.database.nsf.load.model.entity.NSFFile;
import edu.iu.sci2.utilities.nsf.NsfDatabaseFieldNames;

public class AwardOccurence extends RowItem<AwardOccurence> {
	public static final Schema<AwardOccurence> SCHEMA = new Schema<AwardOccurence>(
			false,
			NsfDatabaseFieldNames.AWARD_OCCURRENCES_AWARD_FOREIGN_KEY,
				DerbyFieldType.FOREIGN_KEY,
			NsfDatabaseFieldNames.AWARD_OCCURRENCES_NSF_FILE_FOREIGN_KEY,
				DerbyFieldType.FOREIGN_KEY).
			FOREIGN_KEYS(
				NsfDatabaseFieldNames.AWARD_OCCURRENCES_AWARD_FOREIGN_KEY,
					NsfDatabaseFieldNames.AWARD_TABLE_NAME,
				NsfDatabaseFieldNames.AWARD_OCCURRENCES_NSF_FILE_FOREIGN_KEY,
					NsfDatabaseFieldNames.NSF_FILE_TABLE_NAME
			);
	
	private Award award;
	private NSFFile nsfFile;

	public AwardOccurence(Award award, NSFFile nsfFile) {
		super(createAttributes(award, nsfFile));
		this.award = award;
		this.nsfFile = nsfFile; 
	}

	public Award getAward() {
		return this.award;
	}
	
	public NSFFile getNSFFile() {
		return this.nsfFile;
	}

	/*@Override
	public Object createMergeKey() {
		List<Object> mergeKey = new ArrayList<Object>();
		mergeKey.add(this.award.getPrimaryKey());
		mergeKey.add(this.nsfFile.getPrimaryKey());

		return mergeKey;
	}

	@Override
	public void merge(AwardOccurence otherItem) {
	}*/

	private static Dictionary<String, Object> createAttributes(Award award, NSFFile nsfFile) {
		Dictionary<String, Object> attributes = new Hashtable<String, Object>();
		attributes.put(
			NsfDatabaseFieldNames.AWARD_OCCURRENCES_AWARD_FOREIGN_KEY, award.getPrimaryKey());
		attributes.put(
			NsfDatabaseFieldNames.AWARD_OCCURRENCES_NSF_FILE_FOREIGN_KEY,
			nsfFile.getPrimaryKey());

		return attributes;
	}
}