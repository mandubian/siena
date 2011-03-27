package siena.gae;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import siena.core.options.QueryOption;
import siena.embed.EmbeddedMap;

import com.google.appengine.api.datastore.PreparedQuery;

@EmbeddedMap
public class QueryOptionGaeContext extends QueryOption{
    public static final int ID 	= 0x2001;
	
    public List<String> cursors = new ArrayList<String>();
    // -1 means empty
    public int cursorIdx = -1;
    public boolean useCursor = true;
    // this is the current offset synchronized with the cursor by the PM
    // a flag that can be used when there is no more data to fetch (when previous page is the first one for ex)
    public boolean noMoreDataBefore = false;
    public boolean noMoreDataAfter = false;
    public int realOffset = 0;
    public int realPageSize = 0;
   //public PreparedQuery query;
	public QueryOptionGaeContext() {
		super(ID);
	}
    
	public QueryOptionGaeContext(PreparedQuery query) {
		super(ID);
		//this.query = query;
	}

	public QueryOptionGaeContext(QueryOptionGaeContext option) {
		super(option);
		Collections.copy(this.cursors, option.cursors);
		this.cursorIdx = option.cursorIdx;
		this.useCursor = option.useCursor;
		this.noMoreDataBefore = option.noMoreDataBefore;
		this.noMoreDataAfter = option.noMoreDataAfter;
		this.realOffset = option.realOffset;
		this.realPageSize = option.realPageSize;
		//this.query = option.query;
	}
	
	public void addCursor(String cursor){
		// if cursor in the middle of the list, replace next one
		if(cursorIdx < cursors.size()-1 && cursorIdx>=0){
			cursors.set(cursorIdx+1, cursor);
		}
		// if first or last cursor in the list, adds a new cursor
		else{
			cursors.add(cursorIdx+1, cursor);
		}
	}
	
	public void addAndMoveCursor(String cursor){
		// if cursor in the middle of the list, replace next one
		if(cursorIdx < cursors.size()-1 && cursorIdx>=0){
			cursors.set(++cursorIdx, cursor);
		}
		// if first or last cursor in the list, adds a new cursor
		else{
			cursors.add(++cursorIdx, cursor);
		}
	}
	
	public void setCurrentCursor(String cursor){
		// replaces the cursor at current index (useful for iterators)
		if(cursorIdx!=-1)
			cursors.set(cursorIdx, cursor);
		else {
			addAndMoveCursor(cursor);
		}
	}
	
	public String currentCursor() {
		if(cursorIdx!=-1){
			return cursors.get(cursorIdx);
		}else {
			return null;
		}
	}
	
	public String nextCursor(){
		int sz = cursors.size();
		if(sz==0){
			return null;
		}else {
			if(cursorIdx<sz-1){
				return cursors.get(++cursorIdx);
			}
			else {
				return cursors.get(cursorIdx);
			}
		}
	}
	
	public boolean hasNextCursor(){
		int sz = cursors.size();
		if(sz==0){
			return false;
		}else {
			return true;
		}
	}
	
	public String previousCursor(){
		int sz = cursors.size();
		if(sz==0){
			return null;
		}else {
			if(cursorIdx>0){
				return cursors.get(--cursorIdx);
			}
			else if(cursorIdx==0){
				cursorIdx=-1;
				return null;
			} else {
				return null;
			}
		}
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionGaeContext(this);
	}

	public String toString() {
		return "type:GAE_CONTEXT - state:"+this.state
		 	+ " - realOffset:"+realOffset
		 	+ " - realPageSize:"+realPageSize
			+ " - useCursor:"+useCursor
			+ " - cursorIdx:"+cursorIdx+ " - cursors:"+cursors;
	}
}
