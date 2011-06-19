package siena.embed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import siena.Json;
import siena.Util;

public class JsonSerializerTest extends TestCase {
	
	public void testSimple() throws Exception {
		Date date = createDate();
		Json data = JsonSerializer.serialize(new Contact("Alberto", "Gimeno", Gender.MALE, date, date));
		
		Contact contact = (Contact) JsonSerializer.deserialize(Contact.class, data);
		assertEquals("Alberto", contact.firstName);
		assertNull(contact.foo);
		assertEquals("Gimeno", contact.lastName);
		assertEquals(Gender.MALE, contact.gender);
		assertEquals(date, contact.birthday);
		assertEquals(date, contact.rebirthday);
	}
	
	@SuppressWarnings("unchecked")
	public void testMultiple() throws Exception {
		Date date = createDate();
		Map<String, Contact> contacts = new HashMap<String, Contact>();
		contacts.put("id1", new Contact("Alberto", "Gimeno", Gender.MALE, date, date));
		
		Json data = JsonSerializer.serialize(contacts);
		
		contacts = (Map<String, Contact>) JsonSerializer.deserialize(Util.getField(User.class, "contacts"), data);
		
		Contact contact = contacts.get("id1");
		assertEquals("Alberto", contact.firstName);
		assertNull(contact.foo);
		assertEquals("Gimeno", contact.lastName);
		assertEquals(Gender.MALE, contact.gender);
		assertEquals(date, contact.birthday);
		assertEquals(date, contact.rebirthday);
	}

	private Date createDate() {
		try {
			return new SimpleDateFormat("yyyy/MM/dd").parse("1984/07/16");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

} @EmbeddedList class Contact {
	
	@At(0) public String firstName;
	@At(1) public String foo;
	@At(2) public String lastName;
	@At(3) public Gender gender;
	
	@At(4) @Format("yyyy/MM/dd")
	public Date birthday;
	
	@At(5)
	public Date rebirthday;

	public Contact(String firstName, String lastName, Gender gender, Date birthday, Date rebirthday) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.birthday = birthday;
		this.rebirthday = rebirthday;
	}
	
	public Contact() {
	}
	
} class User {
	
	public Map<String, Contact> contacts;
	
} enum Gender {
	MALE, FEMALE;
}
