package siena.sdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import siena.core.options.QueryOption;
import siena.embed.EmbeddedMap;

import com.google.appengine.api.datastore.PreparedQuery;

@EmbeddedMap
public class QueryOptionSdbContext extends QueryOption{
    public static final int ID 	= 0x3001;
	
    public List<String> tokens = new ArrayList<String>();
    // -1 means empty
    public int tokenIdx = -1;
    public boolean useToken = true;
    // this is the current offset synchronized with the cursor by the PM
    // a flag that can be used when there is no more data to fetch (when previous page is the first one for ex)
    public boolean noMoreDataBefore = false;
    public boolean noMoreDataAfter = false;
    public int realPageSize = 0;
   //public PreparedQuery query;
	public QueryOptionSdbContext() {
		super(ID);
	}
    
	public QueryOptionSdbContext(PreparedQuery query) {
		super(ID);
		//this.query = query;
	}

	public QueryOptionSdbContext(QueryOptionSdbContext option) {
		super(option);
		Collections.copy(this.tokens, option.tokens);
		this.tokenIdx = option.tokenIdx;
		this.useToken = option.useToken;
		this.noMoreDataBefore = option.noMoreDataBefore;
		this.noMoreDataAfter = option.noMoreDataAfter;
		this.realPageSize = option.realPageSize;
		//this.query = option.query;
	}
	
	public void addToken(String token){
		// if cursor in the middle of the list, replace next one
		if(tokenIdx < tokens.size()-1 && tokenIdx>=0){
			tokens.set(tokenIdx+1, token);
		}
		// if first or last cursor in the list, adds a new cursor
		else{
			tokens.add(tokenIdx+1, token);
		}
	}
	
	public void addAndMoveToken(String token){
		// if cursor in the middle of the list, replace next one
		if(tokenIdx < tokens.size()-1 && tokenIdx>=0){
			tokens.set(++tokenIdx, token);
		}
		// if first or last cursor in the list, adds a new cursor
		else{
			tokens.add(++tokenIdx, token);
		}
	}
	
	public void setCurrentToken(String token){
		// replaces the cursor at current index (useful for iterators)
		if(tokenIdx!=-1)
			tokens.set(tokenIdx, token);
		else {
			addAndMoveToken(token);
		}
	}
	
	public String currentToken() {
		if(tokenIdx!=-1){
			return tokens.get(tokenIdx);
		}else {
			return null;
		}
	}
	
	public String nextToken(){
		int sz = tokens.size();
		if(sz==0){
			return null;
		}else {
			if(tokenIdx<sz-1){
				return tokens.get(++tokenIdx);
			}
			else {
				return tokens.get(tokenIdx);
			}
		}
	}
	
	public boolean hasNextToken(){
		int sz = tokens.size();
		if(sz==0){
			return false;
		}else {
			return true;
		}
	}
	
	public String previousToken(){
		int sz = tokens.size();
		if(sz==0){
			return null;
		}else {
			if(tokenIdx>0){
				return tokens.get(--tokenIdx);
			}
			else if(tokenIdx==0){
				tokenIdx=-1;
				return null;
			} else {
				return null;
			}
		}
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionSdbContext(this);
	}

	public String toString() {
		return "type:SDB_CONTEXT - state:"+this.state
		 	+ " - realPageSize:"+realPageSize
			+ " - useToken:"+useToken
			+ " - tokenIdx:"+tokenIdx+ " - tokens:"+tokens;
	}
}
