package siena;

import siena.embed.EmbeddedMap;
import siena.embed.JsonDeserializeAs;
import siena.embed.JsonDumpable;
import siena.embed.JsonRestorable;
import siena.embed.JsonSerializer;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
@JsonDeserializeAs(QueryFilter.QueryFilterJson.class)
public abstract class QueryFilter implements JsonDumpable { 
	
	/* (non-Javadoc)
	 * @see siena.embed.JsonDumpable#dump()
	 */
	public Json dump() {
		QueryFilterJson jsonFilter = new QueryFilterJson();
		jsonFilter.type = this.getClass().getName();
		try {
			jsonFilter.value = JsonSerializer.serializeMap(this);
		}catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
		
		return JsonSerializer.serialize(jsonFilter);
	}

	@EmbeddedMap
	public static class QueryFilterJson implements JsonRestorable<QueryFilter>{
		public String type;
		public Json value;
		
		public QueryFilter restore() {
			try {
				Class<?> clazz = Class.forName(this.type);
				
				QueryFilter filter = (QueryFilter)JsonSerializer.deserializeMap(clazz, this.value);
				return filter;
			}catch(Exception ex) {
				throw new SienaException("Unable to restore QueryFilter", ex);
			}
		}
	}
}
