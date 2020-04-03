package com.aoher.service;

import com.aoher.model.User;

import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Singleton
@Lock
@Path("/user")
@Produces(MediaType.APPLICATION_XML)
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    @PUT
    @Path("/create")
    public User create(@QueryParam("name") String name,
                       @QueryParam("pwd") String pwd,
                       @QueryParam("mail") String mail) {
        User user = new User();
        user.setFullName(name);
        user.setPassword(pwd);
        user.setEmail(mail);

        entityManager.persist(user);
        return user.clone();
    }

    @GET
    @Path("/list")
    public List<User> list(@QueryParam("first") @DefaultValue("0") int first,
                           @QueryParam("max") @DefaultValue("20") int max) {
        List<User> users = new ArrayList<>();
        List<User> found = entityManager
                .createNamedQuery("user.list", User.class)
                .setFirstResult(first).setMaxResults(max)
                .getResultList();

        found.forEach(u -> users.add(u.clone()));
        return users;
    }

    @GET
    @Path("/show/{id}")
    public User find(@PathParam("id") long id) {
        User user = entityManager.find(User.class, id);
        return user == null ? null : user.clone();
    }

    @DELETE
    @Path("/delete/{id}")
    public void delete(@PathParam("id") long id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @POST
    @Path("/update/{id}")
    public Response update(@PathParam("id") long id,
                           @QueryParam("name") String name,
                           @QueryParam("pwd") String pwd,
                           @QueryParam("mail") String mail) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException(format("user id %d not found", id));
        }

        user.setFullName(name);
        user.setPassword(pwd);
        user.setEmail(mail);
        entityManager.merge(user);

        return Response.ok(user.clone()).build();
    }
}
