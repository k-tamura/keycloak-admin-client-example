package org.t246osslab;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

public class KeycloakAdminClientExample {

    public static void main(String[] args) {

        // Create a Keycloak client (builder pattern)
        Keycloak kc = KeycloakBuilder.builder() //
                .serverUrl("http://localhost:8080/auth")
                .realm("master")
                .username("admin")
                .password("password")
                .clientId("admin-cli")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();

        /*
        // Same as above but cannot set RESTEasy client parameter
        Keycloak kc = Keycloak.getInstance(
                "http://localhost:8080/auth",
                "master",
                "admin",
                "password",
                "admin-cli");
        */

        // Create a realm
        String realmName = "realmXX1";
        createRealm(kc, realmName);

        // Create a user
        String useNname = "user1";
        createUser(kc, realmName, useNname);

        // Create a role
        String roleName = "role1";
        createRole(kc, realmName, roleName);

        // Create a client
        String clientName = "client1";
        createClient(kc, realmName, clientName);
    }

    private static void createRealm(Keycloak kc, String realmName) {
        try {
            RealmRepresentation realmRepresentation = new RealmRepresentation();
            realmRepresentation.setRealm(realmName);
            realmRepresentation.setEnabled(Boolean.TRUE);
            kc.realms().create(realmRepresentation);
            System.out.println(realmName + " was created.");
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                System.out.println(realmName + " has already been created.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createUser(Keycloak kc, String realmName, String username) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(Boolean.TRUE);
            user.setUsername(username);
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(Boolean.FALSE);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue("password");
            ArrayList<CredentialRepresentation> credentials = new ArrayList<>();
            credentials.add(passwordCred);
            user.setCredentials(credentials);
            Response result = kc.realm(realmName).users().create(user);
            if (result.getStatus() == Response.Status.CREATED.getStatusCode()) {
                System.out.println(username + " was created.");
            } else if (result.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                System.out.println(username + " has already been created.");
            } else {
                System.out.println("Result status: " + result.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRole(Keycloak kc, String realmName, String roleName) {
        try {
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setName(roleName);
            roleRepresentation.setClientRole(true);
            kc.realm(realmName).roles().create(roleRepresentation);
            System.out.println(roleName + " was created.");
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                System.out.println(roleName + " has already been created.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createClient(Keycloak kc, String realmName, String clientName) {
        try {
            ClientRepresentation clientRepresentation = new ClientRepresentation();
            clientRepresentation.setName(clientName);
            clientRepresentation.setEnabled(Boolean.TRUE);
            kc.realm(realmName).clients().create(clientRepresentation);
            System.out.println(clientName + " was created.");
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                System.out.println(clientName + " has already been created.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
