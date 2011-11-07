/*
 * Copyright 2008 Alberto Gimeno <gimenete at gmail.com>
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
package siena.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import siena.ClassInfo;
import siena.Json;
import siena.SienaException;
import siena.Util;
import siena.core.DecimalPrecision;
import siena.core.Polymorphic;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;

public class GoogleSqlPersistenceManager extends JdbcPersistenceManager {
	private static final String DB = "GOOGLESQL";

	public GoogleSqlPersistenceManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GoogleSqlPersistenceManager(ConnectionManager connectionManager,
			Class<?> listener) {
		super(connectionManager, listener);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int addParameters(Object obj, List<Field> fields, PreparedStatement ps, int i) throws SQLException {
		for (Field field : fields) {
			Class<?> type = field.getType();
			if(ClassInfo.isModel(type) && ! ClassInfo.isEmbedded(field)) {
				JdbcClassInfo ci = JdbcClassInfo.getClassInfo(type);
				Object rel = Util.readField(obj, field);
				for(Field f : ci.keys) {
					if(rel != null) {
						Object value = Util.readField(rel, f);
						if(value instanceof Json)
							value = ((Json)value).toString();
						setParameter(ps, i++, value, f);
					} else {
						setParameter(ps, i++, null, f);
					}
				}
			} else {
				Object value = Util.readField(obj, field);
				if(value != null){
					if(Json.class.isAssignableFrom(type)){
						value = ((Json)value).toString();
					}
					else if(field.getAnnotation(Embedded.class) != null){
						value = JsonSerializer.serialize(value).toString();
					}
					else if(field.getAnnotation(Polymorphic.class) != null){
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutput out;
						try {
							out = new ObjectOutputStream(bos);
							out.writeObject(value);
							out.close();
						} catch (IOException e) {
							throw new SienaException(e);
						}   
						
						value = bos.toByteArray(); 
					}
					else if(Enum.class.isAssignableFrom(type)){
						value = value.toString();
					}
					else if(BigDecimal.class == type){
						DecimalPrecision ann = field.getAnnotation(DecimalPrecision.class);
						if(ann == null) {
							value = (BigDecimal)value;
						}else {
							switch(ann.storageType()){
							case DOUBLE:
								value = ((BigDecimal)value).doubleValue();
								break;
							case STRING:
								value = ((BigDecimal)value).toPlainString();
								break;
							case NATIVE:
								value = (BigDecimal)value;
								break;
							}
						}
					}
				}
				setParameter(ps, i++, value, field);
			}
		}
		return i;
	}

	protected void setParameter(PreparedStatement ps, int index, Object value, Field field) throws SQLException {
		JdbcDBUtils.setObject(ps, index, value, field, DB);
	}
	
}
