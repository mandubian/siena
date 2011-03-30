/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package siena;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Json implements Iterable<Json> {
	
	private Object object;
	private Map<String, Json> map;
	private List<Json> list;
	
	public Json(Object object) {
		if(object == null) {
			this.object = object;
			return;
		}
		
		Class<?> clazz = object.getClass();
		if(object == null
				|| clazz.isPrimitive()
				|| Number.class.isAssignableFrom(clazz)
				|| String.class.isAssignableFrom(clazz)
				|| Boolean.class.isAssignableFrom(clazz)){
			this.object = object;			
		}
		
		else if(clazz.isEnum()) {
			this.object = ((Enum<?>)object).name();
		}		
		else if(Json.class.isAssignableFrom(clazz)){
			// no copy, just reusing other references
			this.map = (((Json)object).map);
			this.list = (((Json)object).list);
			this.object = ((Json)object).object;
		}		
		else if(Map.class.isAssignableFrom(clazz)) {
			map = new HashMap<String, Json>();
			
			Map<?, ?> m = (Map<?, ?>) object;
			for (Map.Entry<?, ?> entry : m.entrySet()) {
				put(entry.getKey().toString(), entry.getValue());
			}
			this.object = null;
		}		
		else if(Collection.class.isAssignableFrom(clazz)) {
			object = ((Collection<?>) object).toArray();
			list = new ArrayList<Json>();
			add((Object[]) object);
			this.object = null;
			return;
		}
		
		else if(clazz.isArray()) {
			list = new ArrayList<Json>();
			add((Object[]) object);
			this.object = null;
		}		

		else {
			throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
		}
	}
	
	private Json() {
	}
	
	public static Json map() {
		Json json = new Json();
		json.map = new HashMap<String, Json>();
		return json;
	}
	
	public static Json sortedMap() {
		Json json = new Json();
		json.map = new TreeMap<String, Json>();
		return json;
	}
	
	public static Json list(Object...objects) {
		Json json = new Json();
		json.list = new ArrayList<Json>();
		json.add(objects);
		return json;
	}
	
	private Json wrap(Object object) {
		if(object instanceof Json)
			return (Json) object;
		return new Json(object);
	}
	
	public Json add(Object...objects) {
		for (Object object : objects) {
			list.add(wrap(object));
		}
		return this;
	}
	
	public Json addAt(int index, Object object) {
		list.add(index, wrap(object));
		return this;
	}
	
	public Json removeAt(int index) {
		list.remove(index);
		return this;
	}
	
	public int indexOf(Object value) {
		return list.indexOf(wrap(value));
	}
	
	public Json get(String key) {
		return map.get(key);
	}
	
	public Json put(String key, Object value) {
		map.put(key, wrap(value));
		return this;
	}
	
	public Json putAll(Json json) {
		Set<String> keys = json.keys();
		for (String key : keys) {
			put(key, json.get(key));
		}
		return this;
	}
	
	public Json addAll(Json json) {
		for (Json js : json) {
			add(js);
		}
		return this;
	}
	
	public Json removeAll(Json json) {
		if(json.isList()) {
			for (Json js : json) {
				remove(js);
			}
		} else if(json.isMap()) {
			for (String key : json.keys()) {
				remove(key);
			}
		}
		return this;
	}
	
	public Json at(int index) {
		// it prevents from generating any exception in case the model has changed dynamically
		// by adding fields to the json embedded list
		if(index >= list.size())
			return new Json();
		return list.get(index);
	}
	
	public Set<String> keys() {
		return map.keySet();
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Object obj) {
		return map.containsValue(wrap(obj));
	}
	
	public boolean contains(Object obj) {
		return list.contains(wrap(obj));
	}
	
	public Collection<Json> values() {
		return map.values();
	}
	
	public boolean remove(Object obj) {
		if(list != null) return list.remove(wrap(obj));
		if(map != null) return map.remove(obj) != null;
		return false;
	}
	
	public void sumIntegers(Json other) {
		for (String key : other.keys()) {
			Json value = get(key);
			if(value == null) {
				put(key, other.get(key));
			} else {
				put(key, value.asLong()+other.get(key).asLong());
			}
		}
	}
	
	public void sumReals(Json other) {
		for (String key : other.keys()) {
			Json value = get(key);
			if(value == null) {
				put(key, other.get(key));
			} else {
				put(key, value.asDouble()+other.get(key).asDouble());
			}
		}
	}
	
	public void sumInteger(String key, long value) {
		if(containsKey(key)) {
			value += get(key).asLong();
		}
		put(key, value);
	}
	
	public void sumReal(String key, double value) {
		if(containsKey(key)) {
			value += get(key).asDouble();
		}
		put(key, value);
	}
	
	public void putDefault(String key, Object value) {
		if(!containsKey(key))
			put(key, value);
	}
	
	public int size() {
		if(map != null) return map.size();
		if(list != null) return list.size();
		return 1;
	}
	
	public Iterator<Json> iterator() {
		if(list != null) return list.iterator();
		if(map != null) return map.values().iterator();
		return null;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	private void formatString(Writer writer, String s) throws IOException {
		writer.write('\"');
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case '\\':
			case '\"':
			case '/':
				writer.write('\\');
				writer.write(c);
				break;
			case '\b':
				writer.write("\\b");
				break;
			case '\f':
				writer.write("\\f");
				break;
			case '\n':
				writer.write("\\n");
				break;
			case '\r':
				writer.write("\\r");
				break;
			case '\t':
				writer.write("\\t");
				break;
			default:
				writer.write(c);
			}
		}
		writer.write("\"");
	}
	
	public void format(Writer writer, Object o) throws IOException {
		if(o instanceof String)
			formatString(writer, (String) o);
		else
			writer.write(o.toString());
	}
	
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			write(writer);
		} catch (IOException e) {
			// Should never happen
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
	
	public void write(Writer writer) throws IOException {
		if(object != null) {
			format(writer, object);
			return;
		}
		if(map != null) {
			if(map.isEmpty()) {
				writer.write("{}");
				return;
			}
			writer.write("{");
			boolean first = true;
			for (Map.Entry<String, Json> entry : map.entrySet()) {
				if(!first) {
					writer.write(", ");
				} else {
					first = false;
				}
				formatString(writer, entry.getKey());
				writer.write(": ");
				entry.getValue().write(writer);
			}
			writer.write("}");
			return;
		}
		if(list != null) {
			if(list.isEmpty()) {
				writer.write("[]");
				return;
			}
			writer.write("[");
			boolean first = true;
			for (Json obj : list) {
				if(!first) {
					writer.write(", ");
				} else {
					first = false;
				}
				obj.write(writer);
			}
			writer.write("]");
			return;
		}
		writer.write("null");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Json other = (Json) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}
	
	public boolean equalsTo(Json other) {
		if(other == null) return false;
		if(this == other) return true;
		if(object != null) {
			if(isNumber()) {
				if(!other.isNumber()) return false;
				return Double.doubleToLongBits(asDouble()) == Double.doubleToLongBits(other.asDouble());
			}
			return object.equals(other.object);
		}
		if(list != null) {
			if(other.list == null) return false;
			int size = list.size();
			if(other.list.size() != size) return false;
			while(size-- > 0) {
				if(!list.get(size).equalsTo(other.list.get(size)))
					return false;
			}
		} else if(map != null) {
			if(other.map == null) return false;
			int size = map.size();
			if(other.map.size() != size) return false;
			for (String key : map.keySet()) {
				Json value = map.get(key);
				if(!value.equalsTo(other.map.get(key)))
					return false;
			}
		}
		return true;
	}
	
	public String str() {
		return asString();
	}
	
	public String asString() {
		return object.toString();
	}
	
	public boolean bool() {
		return asBoolean();
	}
	
	public boolean asBoolean() {
		return ((Boolean) object).booleanValue();
	}
	
	public int asInt() {
		return ((Number) object).intValue();
	}
	
	public short asShort() {
		return ((Number) object).shortValue();
	}
	
	public byte asByte() {
		return ((Number) object).byteValue();
	}
	
	public long asLong() {
		return ((Number) object).longValue();
	}
	
	public Double asDouble() {
		return ((Number) object).doubleValue();
	}
	
	public float asFloat() {
		return ((Number) object).floatValue();
	}
	
	public boolean isNull() {
		return object == null && list == null && map == null;
	}
	
	public boolean isNumber() {
		return object != null && object instanceof Number;
	}
	
	public boolean isBoolean() {
		return object != null && object instanceof Boolean;
	}
	
	public boolean isString() {
		return object != null && object instanceof String;
	}
	
	public boolean isMap() {
		return map != null;
	}
	
	public boolean isList() {
		return list != null;
	}
	
	public Json find(Object...params) {
		Json data = this;
		for (Object object : params) {
			if(object instanceof String && data.isMap()) {
				data = data.get((String) object);
				if(data == null) return null;
			} else if(object instanceof Integer && data.isList()) {
				int value = ((Integer) object).intValue();
				if(value >= data.size()) return null;
				data = data.at(value);
			} else {
				return null;
			}
		}
		return data;
	}
	
	public static Json loads(String s) {
		return new Parser(s).parse();
	}
	
	public static Json load(BufferedReader reader) {
		return new Parser(reader).parse();
	}
	
} class Parser {
	
	private int n;
	private Reader s;
	private char last;
	private boolean more;
	
	public Parser(String s) {
		this.s = new StringReader(s);
	}
	
	public Parser(BufferedReader reader) {
		this.s = reader;
	}
	
	public Json parse() {
		// TODO: try catch with information of position "n".
		// TODO: reached EOF
		try {
			get();
			return next();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Json next() throws IOException {
		char c = last;
		if(c == '\"') {
			String s = "";
			do {
				c = getc();
				if(c == '\\') {
					c = getc();
					switch(c) {
					case 'b':
						s+='\b';
						break;
					case 'f':
						s+='\f';
						break;
					case 'n':
						s+='\n';
						break;
					case 'r':
						s+='\r';
						break;
					case 't':
						s+='\t';
						break;
					case '\"':
						s+='\"';
						break;
					case '\\':
						s+='\\';
						break;
					case '/':
						s+='/';
						break;
					case 'u':
						char u = (char) (Integer.parseInt(
								new String(new char[]{
										getc(), getc(), getc(), getc()}), 16));
						s+=u;
						break;
					default:
						throw new RuntimeException("Invalid escape character: \\"+c+" at position "+n);
					}
				} else if(c == '\"') {
					if(more()) get();
					return new Json(s);
				} else {
					s += c;
				}
			} while(true);
		} else if(c == '[') {
			Json result = Json.list();
			get();
			c = ignoreWS();
			if(c == ']') {
				if(more()) get();
				return result;
			}
			do {
				result.add(next());
				c = ignoreWS();
				if(c == ']') {
					if(more()) get();
					return result;
				}
				if(c != ',') throw new RuntimeException("expected ',' or ']' at character "+n);
				get();
				c = ignoreWS();
			} while(true);
		} else if(c == '{') {
			Json result = Json.map();
			get();
			c = ignoreWS();
			if(c == '}') {
				if(more()) get();
				return result;
			}
			do {
				Json k = next();
				if(!k.isString())
					throw new RuntimeException("find non-string key at character "+n);
				String key = k.str();
				c = ignoreWS();
				if(c != ':') throw new RuntimeException("expected ':' at character "+n);
				get();
				c = ignoreWS();
				Json value = next();
				result.put(key, value);
				c = ignoreWS();
				if(c == '}') {
					if(more()) get();
					return result;
				}
				if(c != ',') throw new RuntimeException("expected: ',' or '}'' at character "+n);
				get();
				c = ignoreWS();
			} while(true);
		} else if(c == 't') {
			if(getc() != 'r' || getc() != 'u' || getc() != 'e')
				throw new RuntimeException("expected 'true' at character "+n);
			if(more()) get();
			return new Json(Boolean.TRUE);
		} else if(c == 'f') {
			if(getc() != 'a' || getc() != 'l' || getc() != 's' || getc() != 'e')
				throw new RuntimeException("expected 'false' at character "+n);
			if(more()) get();
			return new Json(Boolean.FALSE);
		} else if(c == 'n') {
			if(getc() != 'u' || getc() != 'l' || getc() != 'l')
				throw new RuntimeException("expected 'true' at character "+n);
			if(more()) get();
			return new Json(null);
		} else if(c == '-' || (c >= '0' && c <= '9')) {
			String s = "";
			boolean d = false;
			do {
				s+=c;
				if(!more()) break;
				c = getc();
				if(c == '.' || c == 'e' || c == 'E') d = true;
			} while((c >= '0' && c <= '9') || c == 'e' ||
					c == 'E' || c == '.' || c == '+' || c == '-');
			if(d)
				return new Json(Double.parseDouble(s));
			return new Json(Long.parseLong(s));
		} else {
			throw new RuntimeException("expected: '{', '[', '\"', true, false, null, or a number at "+n);
		}
	}
	
	private char ignoreWS() throws IOException {
		char c = last;
		while(c == ' ' || c == '\n' || c == '\r' || c == '\t') {
			c = getc();
		}
		return c;
	}
	
	private void get() throws IOException {
		int r = s.read();
		n++;
		more = r != -1;
		last = (char) r;
	}
	
	private boolean more() {
		return more;
	}
	
	private char getc() throws IOException {
		get(); return last;
	}
	
}
