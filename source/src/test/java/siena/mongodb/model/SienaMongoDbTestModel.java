package siena.mongodb.model;

import java.util.Date;
import java.util.List;

import siena.Id;
import siena.Model;
import siena.Query;

/**
 * Only test Model class for MongoDBPersistenceManagerTest
 * 
 * @author T.hagikura
 *
 */
public final class SienaMongoDbTestModel extends Model {
	@Id // in Siena-MongoDb, @Id annotation column must be Object not Long, since generated _id from java-mongo-driver contains alphabetical characters.
	public Object id;
	public String stringField;
	public Integer integerField;
	public Long longField;
	public Double doubleField;
	public Date dateField;
	
	public static Query<SienaMongoDbTestModel> all() {
		return Model.all(SienaMongoDbTestModel.class);
	}

	public static void deleteAll() {
		List<SienaMongoDbTestModel> models = all().fetch();
		for (SienaMongoDbTestModel model : models) {
			model.delete();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dateField == null) ? 0 : dateField.hashCode());
		result = prime * result + ((doubleField == null) ? 0 : doubleField.hashCode());
		result = prime * result + ((integerField == null) ? 0 : integerField.hashCode());
		result = prime * result + ((longField == null) ? 0 : longField.hashCode());
		result = prime * result + ((stringField == null) ? 0 : stringField.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SienaMongoDbTestModel [id=" + id + ", stringField=" + stringField + ", integerField=" + integerField
				+ ", longField=" + longField + ", doubleField=" + doubleField + ", dateField=" + dateField + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SienaMongoDbTestModel other = (SienaMongoDbTestModel) obj;
		if (dateField == null) {
			if (other.dateField != null)
				return false;
		} else if (!dateField.equals(other.dateField))
			return false;
		if (doubleField == null) {
			if (other.doubleField != null)
				return false;
		} else if (!doubleField.equals(other.doubleField))
			return false;
		if (integerField == null) {
			if (other.integerField != null)
				return false;
		} else if (!integerField.equals(other.integerField))
			return false;
		if (longField == null) {
			if (other.longField != null)
				return false;
		} else if (!longField.equals(other.longField))
			return false;
		if (stringField == null) {
			if (other.stringField != null)
				return false;
		} else if (!stringField.equals(other.stringField))
			return false;
		return true;
	}
	
}
