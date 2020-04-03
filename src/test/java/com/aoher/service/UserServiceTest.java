package com.aoher.service;

import com.aoher.model.User;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class UserServiceTest {

    private static final String LOCALHOST_URL = "http://localhost:4204/rest-on-ejb";

    private static Context context;
    private static UserService userService;
    private static List<User> users;

    @BeforeClass
    public static void setUp() throws NamingException {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        context = EJBContainer.createEJBContainer(properties).getContext();

        userService = (UserService) context.lookup("java:global/rest-on-ejb/UserService");

        users = new ArrayList<>();
        users.add(userService.create("foo", "foopwd", "foo@foo.com"));
        users.add(userService.create("bar", "barpwd", "bar@bar.com"));
    }

    @AfterClass
    public static void tearDown() throws NamingException {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void create() {
        WebClient.create(LOCALHOST_URL)
                .path("/user/create")
                .query("name", "dummy")
                .query("pwd", "unbreakable")
                .query("mail", "foo@bar.fr")
                .put("{}");

        List<User> list = userService.list(0, 100);
        for (User u : list) {
            if (!users.contains(u)) {
                userService.delete(u.getId());
                return;
            }
        }
        fail("user was not added");
    }

    @Test
    public void list() {
        String userList = WebClient.create(LOCALHOST_URL)
                .path("/user/list")
                .get(String.class);

        assertEquals(userList,
                inline(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<users>"
                + "  <user>"
                + "    <email>bar@bar.com</email>"
                + "    <fullname>bar</fullname>"
                + "    <id>2</id>"
                + "    <password>barpwd</password>"
                + "  </user>"
                + "  <user>"
                + "    <email>foo@foo.com</email>"
                + "    <fullname>foo</fullname>"
                + "    <id>1</id>"
                + "    <password>foopwd</password>"
                + "  </user>"
                + "</users>"),
                inline(userList)
        );
    }

    @Test
    public void find() {
        User user = WebClient.create(LOCALHOST_URL)
                .path("/user/show/" + users.iterator().next().getId())
                .get(User.class);

        assertEquals("foo", user.getFullName());
        assertEquals("foopwd", user.getPassword());
        assertEquals("foo@foo.com", user.getEmail());
    }

    @Test
    public void delete() {
        User user = userService.create("todelete", "dontforget", "delete@me.com");

        WebClient.create(LOCALHOST_URL)
                .path("/user/delete/" + user.getId())
                .delete();

        user = userService.find(user.getId());
        assertNull(user);

    }

    @Test
    public void update() throws JAXBException {
        User created = userService.create("name", "pwd", "mail");

        Response response = WebClient.create(LOCALHOST_URL)
                .path("/user/update/" + created.getId())
                .query("name", "corrected")
                .query("pwd", "userpwd")
                .query("mail", "it@is.ok")
                .post(null);

        JAXBContext ctx = JAXBContext.newInstance(User.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        User modified = (User) unmarshaller.unmarshal((InputStream) response.getEntity());

        assertEquals("corrected", modified.getFullName());
        assertEquals("userpwd", modified.getPassword());
        assertEquals("it@is.ok", modified.getEmail());

        userService.delete(created.getId());
    }

    private static String inline(String s) {
        return s.replace(System.getProperty("line.separator"), "")
                .replace("\n", "")
                .replace(" ", "")
                .replace("\t", "");
    }
}