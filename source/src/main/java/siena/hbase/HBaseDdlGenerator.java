package siena.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import siena.ClassInfo;
import siena.Model;
import siena.SienaException;

public class HBaseDdlGenerator {
	
	private List<Class<? extends Model>> classes = new ArrayList<Class<? extends Model>>();
	
	public void addTable(Class<? extends Model> clazz) {
		classes.add(clazz);
	}
	
	public void updateSchema() throws IOException {
		Configuration config = HBaseConfiguration.create();
		HBaseAdmin admin = new HBaseAdmin(config);
		HTableDescriptor[] descriptors = admin.listTables();
		
		List<String> tables = new ArrayList<String>();
		
		for (HTableDescriptor hTableDescriptor : descriptors) {
			tables.add(hTableDescriptor.getNameAsString());
		}
		
		for (Class<?> clazz : classes) {
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			String tableName = info.tableName;
			
			if(!tables.contains(tableName)) {
				// create table
				HTableDescriptor descriptor = createTable(tableName);
				admin.createTable(descriptor);
			}
			tables.remove(tableName);
		}
		
		for (String table : tables) {
			admin.disableTable(table);
			admin.deleteTable(table);
		}
	}
	
	private HTableDescriptor createTable(String tableName) {
		HTableDescriptor descriptor = new HTableDescriptor(tableName);
		
		HColumnDescriptor columnDescriptor = new HColumnDescriptor("string:");
		descriptor.addFamily(columnDescriptor);
		
		return descriptor;
	}
	
	public void dropTables() {
		HBaseConfiguration config = new HBaseConfiguration();
		try {
			HBaseAdmin admin = new HBaseAdmin(config);
			HTableDescriptor[] descriptors = admin.listTables();
			for (HTableDescriptor hTableDescriptor : descriptors) {
				String name = hTableDescriptor.getNameAsString();
				admin.disableTable(name);
				admin.deleteTable(name);
			}
		} catch(IOException e) {
			throw new SienaException(e);
		}
	}

}
