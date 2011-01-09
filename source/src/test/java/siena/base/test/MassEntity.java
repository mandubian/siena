/*
 * Copyright 2008-2010 Alberto Gimeno <gimenete at gmail.com>
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
package siena.base.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Json;
import siena.Max;
import siena.Table;
import siena.embed.Embedded;

@Table("massentity")
public class MassEntity {

	@Id(Generator.AUTO_INCREMENT)
	public String id;
	
	public byte 		typeByte;
	public short 		typeShort;
	public int 			typeInt;
	public long 		typeLong;
	public float 		typeFloat;
	public double 		typeDouble;

	@Max(100)
	public String 		typeString;
	@Max(1000)
	public String 		typeLargeString;
	
	public Date 		typeDate;
	
	public Json 		typeJson;

	public byte[] 		typeBlob;
	
	@Embedded
	public Map<String, Contact> contacts;
	
	@Embedded
	public List<Address> addresses;

	@Override
	public String toString() {
		return "DataTypes [id=" + id + ", typeByte=" + typeByte + ", typeDate="
				+ typeDate + ", typeDouble=" + typeDouble + ", typeFloat="
				+ typeFloat + ", typeInt=" + typeInt + ", typeJson=" + typeJson
				+ ", typeLargeString=" + typeLargeString + ", typeLong="
				+ typeLong + ", typeShort=" + typeShort + ", typeString="
				+ typeString + ", typeBlob.length=" + typeBlob.length + "]";
	}
	

}
