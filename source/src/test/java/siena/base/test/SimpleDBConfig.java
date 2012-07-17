package siena.base.test;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class SimpleDBConfig {

    public static Properties getSienaAWSProperties() throws java.io.IOException
    {
    	Properties p = new Properties();
    	InputStream is = null;
    	//is = new FileInputStream("/Users/gregorymaertens/sienaaws.properties");
    	is = new FileInputStream("/home/mandubian/work/aws/siena-aws.properties");
	    p.load(is);
    	return p;
    }
	
}
