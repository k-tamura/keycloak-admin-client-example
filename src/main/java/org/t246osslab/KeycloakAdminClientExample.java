package org.t246osslab;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.*;
import org.keycloak.util.JsonSerialization;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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

        // See the contents of the access token
        printAccessToken(kc);

        // Create a realm
        String realmName = "realm1";
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

        // Force logout all users in the realm
        kc.realm(realmName).logoutAll();
    }

    private static void printAccessToken(Keycloak kc) {
        try {
            String accessTokenString = kc.tokenManager().getAccessToken().getToken();
            System.out.println("accessTokenString: " + accessTokenString);
            JWSInput input = new JWSInput(accessTokenString);
            AccessToken accessToken = input.readJsonContent(AccessToken.class);
            System.out.println("subject: " + accessToken.getSubject());
            System.out.println("preferredUsername: " + accessToken.getPreferredUsername());
            System.out.println("givenName: " + accessToken.getGivenName());
        } catch (ClientErrorException e) {
            handleClientErrorException(e);
        } catch (JWSInputException e) {
            e.printStackTrace();
        }
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
                handleClientErrorException(e);
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClientErrorException) {
                handleClientErrorException((ClientErrorException) cause);
            } else {
                e.printStackTrace();
            }
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
            Response response = kc.realm(realmName).users().create(user);
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                System.out.println(username + " was created.");
            } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                System.out.println(username + " has already been created.");
            } else {
                System.out.println("Result status: " + response.getStatus());
            }
        } catch (ClientErrorException e) {
            handleClientErrorException(e);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClientErrorException) {
                handleClientErrorException((ClientErrorException) cause);
            } else {
                e.printStackTrace();
            }
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
                handleClientErrorException(e);
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClientErrorException) {
                handleClientErrorException((ClientErrorException) cause);
            } else {
                e.printStackTrace();
            }
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
                handleClientErrorException(e);
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClientErrorException) {
                handleClientErrorException((ClientErrorException) cause);
            } else {
                e.printStackTrace();
            }
        }
    }

    private static void handleClientErrorException(ClientErrorException e) {
        e.printStackTrace();
        Response response = e.getResponse();
        try {
            System.out.println("status: " + response.getStatus());
            System.out.println("reason: " + response.getStatusInfo().getReasonPhrase());
            Map error = JsonSerialization.readValue((ByteArrayInputStream) response.getEntity(), Map.class);
            System.out.println("error: " + error.get("error"));
            System.out.println("error_description: " + error.get("error_description"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
