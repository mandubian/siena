/*
 * Copyright 2011 Roch Delsalle <rdelsalle at gmail.com>
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
package siena.base.test.model;

import java.math.BigDecimal;

import siena.ExternalStorage;
import siena.ExternalStorage.ExternalStorageType;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.core.DecimalPrecision;
import siena.core.DecimalPrecision.StorageType;

@Table("external_storage")
public class ExternalStorageModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@ExternalStorage(ExternalStorageType.s3)
	public S3Blob blob;

	public ExternalStorageModel() {}

	public ExternalStorageModel(Blob blob) {
		this.blob = blob;
	}
}
