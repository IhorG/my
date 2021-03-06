package com.company;

import javax.persistence.*;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATest");
        EntityManager em = emf.createEntityManager();
        try {
            Group g1 = new Group("Group-1", "My group");
            Group g2 = new Group("Group-2", "Another group");
            Client c = null;
            Address a;
            long cid = -1, gid = -1;
            String name;

            try {
                em.getTransaction().begin();

                // persist groups
                em.persist(g1);
                em.persist(g2);

                System.out.println(gid = g1.getId());

                // generate users
                for (int i = 0; i < 5; i++) {
                    a = new Address("UA", "Kyiv", RND.nextInt(300));
                    em.persist(a);

                    name = randomName();
                    c = new Client(name, name.toLowerCase() + "@mydomain.com", randomPhone());
                    c.setGroup(g1);
                    c.setAddress(a);
                    em.persist(c);
                }
                for (int i = 0; i < 5; i++) {
                    name = randomName();
                    c = new Client(name, name.toLowerCase() + "@mydomain.com", randomPhone());
                    g2.addClient(c);
                    em.persist(c);
                }
                cid = c.getId(); // last client id

                em.getTransaction().commit();
            } catch (Exception ex) {
                em.getTransaction().rollback();
                ex.printStackTrace();
            }

            // find group by id
            Group group = em.find(Group.class, gid);
            for (Client cli : group.getClients())
                System.out.println("Name: " + cli.getName() + ", E-mail: " + cli.getEmail() + ", Address: "
                        + cli.getAddress());

            // find client by id
            Client client = em.find(Client.class, cid);
            System.out.println("Name: " + client.getName() + ", Group: " + c.getGroup().getName());

            // modify record
            try {
                em.getTransaction().begin();
                client.setEmail("newaddress@gmail.com");
                client.setPhone("0440987654");
                em.getTransaction().commit();
            } catch (Exception ex) {
                ex.printStackTrace();
                em.getTransaction().rollback();
            }

            // delete group with clients
            try {
                group = em.find(Group.class, gid);
                em.getTransaction().begin();
                em.remove(group);
                em.getTransaction().commit();
            } catch (Exception ex){
                ex.printStackTrace();
                em.getTransaction().rollback();
            }

            // select all clients
            Query query = em.createQuery("SELECT c FROM Client c", Client.class);
            List<Client> list = (List<Client>) query.getResultList();
            System.out.println("All clients:");
            for (Client cli : list)
                System.out.println("\tName: " + cli.getName() + ", Group: " + cli.getGroup().getName());

            // select where
            try {
                query = em.createQuery("SELECT c FROM Client c WHERE c.email = :email", Client.class);
                query.setParameter("email", "petr@mydomain.com");
                Client qc = (Client) query.getSingleResult();
                System.out.println(">>> Name: " + qc.getName() + ", Group: " + qc.getGroup().getName());
            } catch (NoResultException ex) {
                System.out.println(">>> Not found!");
            } catch (NonUniqueResultException ex) {
                System.out.println(">>> Non unique result!");
            }

            // generate courses
            Course course = new Course("Java Start");
            try {
                em.getTransaction().begin();
                em.persist(course);
                for (Client cli : list) {
                    cli.addCourse(course);
                }
                em.getTransaction().commit();
            } catch (Exception ex){
                ex.printStackTrace();
                em.getTransaction().rollback();
            }

            VipClient vc = new VipClient();
            vc.setPhone("12345678");
            try {
                em.getTransaction().begin();
                em.persist(vc);
                em.getTransaction().commit();
            } catch (Exception ex){
                ex.printStackTrace();
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
            emf.close();
        }
    }

    static final String[] NAMES = {"Ivan", "Petr", "Andrey", "Vsevolod", "Dmitriy"};
    static final Random RND = new Random();

    static String randomName() {
        return NAMES[RND.nextInt(NAMES.length)];
    }

    static String randomPhone() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++)
            sb.append(RND.nextInt(10));
        return sb.toString();
    }
}
