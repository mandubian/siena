package siena.base.test;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.Model;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.base.test.model.EmbeddedContainerModel;
import siena.base.test.model.EmbeddedContainerModelJava;
import siena.base.test.model.EmbeddedContainerModelNative;
import siena.base.test.model.EmbeddedContainerNative;
import siena.base.test.model.EmbeddedModel;
import siena.base.test.model.EmbeddedNative;
import siena.base.test.model.EmbeddedNative.MyEnum;
import siena.base.test.model.EmbeddedSubModel;

public abstract class BaseEmbeddedTest extends TestCase {
	
	protected PersistenceManager pm;

	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(EmbeddedModel.class);
		classes.add(EmbeddedSubModel.class);
		classes.add(EmbeddedContainerModel.class);
		classes.add(EmbeddedContainerModelJava.class);
		classes.add(EmbeddedContainerModelNative.class);
		classes.add(EmbeddedContainerNative.class);
		classes.add(EmbeddedNative.class);
		
		pm = createPersistenceManager(classes);
		PersistenceManagerFactory.install(pm, classes);
			
		for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
		}

	}

	public void testEmbeddedModel() {
		EmbeddedModel embed = new EmbeddedModel();
		embed.id = "embed";
		embed.alpha = "test";
		embed.beta = 123;
		embed.setGamma(true);
		pm.insert(embed);
		
		EmbeddedModel embed2 = new EmbeddedModel();
		embed2.id = "embed2";
		embed2.alpha = "test2";
		embed2.beta = 1234;
		embed2.setGamma(true);
		pm.insert(embed2);
		
		EmbeddedContainerModel container = new EmbeddedContainerModel();
		container.id = "container";
		container.embed = embed;
		container.embeds = new ArrayList<EmbeddedModel>();
		container.embeds.add(embed);
		container.embeds.add(embed2);
		pm.insert(container);

		EmbeddedContainerModel afterContainer = pm.getByKey(EmbeddedContainerModel.class, container.id);
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.id, afterContainer.embed.id);
		assertEquals(null, afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
		int i=0;
		for(EmbeddedModel mod: afterContainer.embeds){
			assertEquals(container.embeds.get(i++).id, mod.id);
		}
		assertEquals(embed.isGamma(), afterContainer.embed.isGamma());
	}
	
	public void testEmbeddedModelJava() {
		EmbeddedModel embed = new EmbeddedModel();
		embed.id = "embed";
		embed.alpha = "test";
		embed.beta = 123;
		embed.setGamma(true);
		pm.insert(embed);
		
		EmbeddedModel embed2 = new EmbeddedModel();
		embed2.id = "embed2";
		embed2.alpha = "test2";
		embed2.beta = 1234;
		embed2.setGamma(true);
		pm.insert(embed2);
		
		EmbeddedContainerModelJava container = new EmbeddedContainerModelJava();
		container.id = "container";
		container.embed = embed;
		container.embeds = new ArrayList<EmbeddedModel>();
		container.embeds.add(embed);
		container.embeds.add(embed2);
		pm.insert(container);

		EmbeddedContainerModelJava afterContainer = pm.getByKey(EmbeddedContainerModelJava.class, container.id);
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.id, afterContainer.embed.id);
		
		// doesn't ignore @EmbedIgnore as it is not possible to have fine grain in Java serialization
		assertEquals("test", afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
		int i=0;
		for(EmbeddedModel mod: afterContainer.embeds){
			assertEquals(container.embeds.get(i++).id, mod.id);
		}
		assertEquals(embed.isGamma(), afterContainer.embed.isGamma());
	}
	
	public void testEmbeddedNative() {
		EmbeddedNative embed = new EmbeddedNative();
		embed.alpha = "test";
		embed.beta = 123;
		embed.setGamma(true);
		embed.delta = 456L;
		embed.eta = new ArrayList<String>() {{ add("one"); add("two"); }};
		embed.myEnum = MyEnum.ONE;
		embed.big = new BigDecimal("12345678.12345678");
		embed.jsonEmbed = new EmbeddedNative.SubEmbed();
		embed.jsonEmbed.str = "test";
		embed.jsonEmbed.l = 123L;
		embed.javaEmbed = new EmbeddedNative.SubEmbed();
		embed.javaEmbed.str = "test";
		embed.javaEmbed.l = 123L;
		embed.nativeEmbed = new EmbeddedNative.SubEmbed();
		embed.nativeEmbed.str = "test";
		embed.nativeEmbed.l = 123L;
		
		EmbeddedContainerNative container = new EmbeddedContainerNative();
		container.id = "container";
		container.normal = "string";
		container.embed = embed;
		pm.save(container);
		
		EmbeddedContainerNative afterContainer = Model.getByKey(EmbeddedContainerNative.class, container.id);
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertEquals(container.normal, afterContainer.normal);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.alpha, afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
		assertEquals(embed.isGamma(), afterContainer.embed.isGamma());
		assertEquals(embed.myEnum, afterContainer.embed.myEnum);
		assertEquals(embed.big, afterContainer.embed.big);
		assertEquals(embed.jsonEmbed.str, afterContainer.embed.jsonEmbed.str);
		assertEquals(embed.jsonEmbed.l, afterContainer.embed.jsonEmbed.l);
		assertEquals(embed.javaEmbed.str, afterContainer.embed.javaEmbed.str);
		assertEquals(embed.javaEmbed.l, afterContainer.embed.javaEmbed.l);
		assertEquals(embed.nativeEmbed.str, afterContainer.embed.nativeEmbed.str);
		assertEquals(embed.nativeEmbed.l, afterContainer.embed.nativeEmbed.l);
	}
	
	public void testEmbeddedNativeFilter() {
		List<EmbeddedContainerNative> containers = new ArrayList<EmbeddedContainerNative>();
		for(int i=0; i<100; i++){
			EmbeddedNative embed = new EmbeddedNative();
			embed.alpha = "test"+i;
			embed.beta = 123;
			embed.setGamma(true);
			embed.delta = 456L;
			embed.eta = new ArrayList<String>() {{ add("one"); add("two"); }};
			embed.myEnum = MyEnum.ONE;
			embed.big = new BigDecimal("12345678.12345678");
			embed.jsonEmbed = new EmbeddedNative.SubEmbed();
			embed.jsonEmbed.str = "test";
			embed.jsonEmbed.l = 123L;
			embed.javaEmbed = new EmbeddedNative.SubEmbed();
			embed.javaEmbed.str = "test";
			embed.javaEmbed.l = 123L;
			embed.nativeEmbed = new EmbeddedNative.SubEmbed();
			embed.nativeEmbed.str = "subtest"+i;
			embed.nativeEmbed.l = 123L;
			
			EmbeddedContainerNative container = new EmbeddedContainerNative();
			container.id = "container"+i;
			container.normal = "string";
			container.embed = embed;
			pm.insert(container);
			
			containers.add(container);
		}
		
		
		
		EmbeddedContainerNative afterContainer = Model.all(EmbeddedContainerNative.class).filter("embed.alpha", "test56").get();
		assertNotNull(afterContainer);
		assertEquals(containers.get(56).id, afterContainer.id);
		assertEquals(containers.get(56).normal, afterContainer.normal);
		assertNotNull(afterContainer.embed);
		assertEquals(containers.get(56).embed.alpha, afterContainer.embed.alpha);
		assertEquals(containers.get(56).embed.beta, afterContainer.embed.beta);
		assertEquals(containers.get(56).embed.isGamma(), afterContainer.embed.isGamma());
		assertEquals(containers.get(56).embed.myEnum, afterContainer.embed.myEnum);
		assertEquals(containers.get(56).embed.big, afterContainer.embed.big);
		assertEquals(containers.get(56).embed.jsonEmbed.str, afterContainer.embed.jsonEmbed.str);
		assertEquals(containers.get(56).embed.jsonEmbed.l, afterContainer.embed.jsonEmbed.l);
		assertEquals(containers.get(56).embed.javaEmbed.str, afterContainer.embed.javaEmbed.str);
		assertEquals(containers.get(56).embed.javaEmbed.l, afterContainer.embed.javaEmbed.l);
		assertEquals(containers.get(56).embed.nativeEmbed.str, afterContainer.embed.nativeEmbed.str);
		assertEquals(containers.get(56).embed.nativeEmbed.l, afterContainer.embed.nativeEmbed.l);
		
		afterContainer = Model.all(EmbeddedContainerNative.class).filter("embed.nativeEmbed.str", "subtest64").get();
		assertNotNull(afterContainer);
		assertEquals(containers.get(64).id, afterContainer.id);
		assertEquals(containers.get(64).normal, afterContainer.normal);
		assertNotNull(afterContainer.embed);
		assertEquals(containers.get(64).embed.alpha, afterContainer.embed.alpha);
		assertEquals(containers.get(64).embed.beta, afterContainer.embed.beta);
		assertEquals(containers.get(64).embed.isGamma(), afterContainer.embed.isGamma());
		assertEquals(containers.get(64).embed.myEnum, afterContainer.embed.myEnum);
		assertEquals(containers.get(64).embed.big, afterContainer.embed.big);
		assertEquals(containers.get(64).embed.jsonEmbed.str, afterContainer.embed.jsonEmbed.str);
		assertEquals(containers.get(64).embed.jsonEmbed.l, afterContainer.embed.jsonEmbed.l);
		assertEquals(containers.get(64).embed.javaEmbed.str, afterContainer.embed.javaEmbed.str);
		assertEquals(containers.get(64).embed.javaEmbed.l, afterContainer.embed.javaEmbed.l);
		assertEquals(containers.get(64).embed.nativeEmbed.str, afterContainer.embed.nativeEmbed.str);
		assertEquals(containers.get(64).embed.nativeEmbed.l, afterContainer.embed.nativeEmbed.l);
	}
	
	public void testEmbeddedModelNative() {
		EmbeddedModel embed = new EmbeddedModel();
		embed.id = "embed";
		embed.alpha = "test";
		embed.beta = 123;
		embed.setGamma(true);
		embed.sub = new EmbeddedSubModel();
		embed.sub.id = "sub";
		embed.sub.parent = embed;
		
		pm.insert(embed);
		
		EmbeddedContainerModelNative container = new EmbeddedContainerModelNative();
		container.id = "container";
		container.embed = embed;
		pm.insert(container);

		EmbeddedContainerModelNative afterContainer = pm.getByKey(EmbeddedContainerModelNative.class, container.id);
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.id, afterContainer.embed.id);
		
		// doesn't ignore @EmbedIgnore as it is not possible to have fine grain in Java serialization
		assertEquals("test", afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
		assertEquals(embed.isGamma(), afterContainer.embed.isGamma());
		assertEquals("sub", afterContainer.embed.sub.id);
		assertNull(afterContainer.embed.sub.parent);
	}
}
