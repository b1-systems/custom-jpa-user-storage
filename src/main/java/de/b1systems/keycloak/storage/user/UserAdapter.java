/* Copyright 2024  B1 Systems GmbH <info@b1-systems.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */

package de.b1systems.keycloak.storage.user;

import jakarta.persistence.EntityManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.RoleProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;

public class UserAdapter
extends AbstractUserAdapterFederatedStorage
{
    private static final Logger logger = Logger
        .getLogger(UserAdapter.class);
    protected UserEntity entity;
    protected String keycloakId;
    protected EntityManager em;
    private RoleProvider roles;
    private boolean readOnly;
    private boolean importRealmRoles;
    private boolean importClientRoles;
    private boolean createRealmRoles;
    private boolean createClientRoles;

    public UserAdapter(
        KeycloakSession session,
        RealmModel realm,
        ComponentModel model,
        UserEntity entity
    ) {
        super(session, realm, model);

        this.entity = entity;
        this.keycloakId = StorageId.keycloakId(model, entity.getId());
        this.roles = session.roles();
        this.em = session
            .getProvider(JpaConnectionProvider.class, "custom-jpa-user-datasource")
            .getEntityManager();
        String ro = model.getConfig().getFirst("readOnly");
        this.readOnly = ro == null || ro.equals("true");
        String ic = model.getConfig().getFirst("importClientRoles");
        this.importClientRoles = ic != null && ic.equals("true");
        String cc = model.getConfig().getFirst("createClientRoles");
        this.createClientRoles = cc != null && cc.equals("true");
        String ir = model.getConfig().getFirst("importRealmRoles");
        this.importRealmRoles = ir != null && ir.equals("true");
        String cr = model.getConfig().getFirst("createRealmRoles");
        this.createRealmRoles = cr != null && cr.equals("true");
    }

    public String getPasswordHash() {
        return entity.getPasswordHash();
    }

    public void setPasswordHash(String password_hash) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        entity.setPasswordHash(password_hash);
        em.persist(entity);
    }

    @Override
    public String getUsername() {
        return entity.getUsername();
    }

    @Override
    public void setUsername(String username) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        entity.setUsername(username);
    }

    @Override
    public void setCreatedTimestamp(Long createdTimestamp) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        entity.setCreatedTimestamp(createdTimestamp);
    }

    @Override
    public Long getCreatedTimestamp() {
        return entity.getCreatedTimestamp();
    }

    @Override
    public void setEmail(String email) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        entity.setEmail(email);
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public boolean isEmailVerified() {
        return entity.getEmailVerified();
    }

    @Override
    public void setEmailVerified(boolean emailVerified) {
        entity.setEmailVerified(emailVerified);
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        if (name.equals("firstName")) {
            entity.setFirstName(value);
        } else if (name.equals("lastName")) {
            entity.setLastName(value);
        } else if (name.equals("phoneNumber")) {
            entity.setPhoneNumber(value);
        } else {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        if (name.equals("firstName")) {
            entity.setFirstName(null);
        } else if (name.equals("lastName")) {
            entity.setLastName(null);
        } else if (name.equals("phoneNumber")) {
            entity.setPhoneNumber(null);
        } else {
            super.removeAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if(readOnly) {
            throw new ReadOnlyException("User is read-only");
        }

        if (name.equals("firstName")) {
            entity.setFirstName(values.get(0));
        } else if (name.equals("lastName")) {
            entity.setLastName(values.get(0));
        } else if (name.equals("phoneNumber")) {
            entity.setPhoneNumber(values.get(0));
        } else {
            super.setAttribute(name, values);
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        if (name.equals("firstName")) {
            return entity.getFirstName();
        } if (name.equals("lastName")) {
            return entity.getLastName();
        } if (name.equals("phoneNumber")) {
            return entity.getPhoneNumber();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add("firstName", entity.getFirstName());
        all.add("lastName", entity.getLastName());
        all.add("phoneNumber", entity.getPhoneNumber());
        return all;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        if (name.equals("firstName")) {
            List<String> firstName = new LinkedList<>();
            firstName.add(entity.getFirstName());
            return firstName.stream();
        } else if (name.equals("lastName")) {
            List<String> lastName = new LinkedList<>();
            lastName.add(entity.getLastName());
            return lastName.stream();
        } else if (name.equals("phoneNumber")) {
            List<String> phoneNumber = new LinkedList<>();
            phoneNumber.add(entity.getPhoneNumber());
            return phoneNumber.stream();
        } else {
            return super.getAttributeStream(name);
        }
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        Stream<RoleModel> roleMappings = super.getRoleMappingsStream();

        if(!this.importClientRoles) {
            logger.warnf("Import of client roles disabled for this provider; skipping.");
	}
	else {
            for (ClientRoleEntity clientRole : entity.getClientRoles()) {
                String clientId = clientRole.getClient();
                ClientModel client = realm.getClientByClientId(clientId);

                if(client==null) {
                    logger.warnf(
                        "User %s requests client role %s.%s, " +
                        "but client %s does not exist; " +
                        "client role not assigned.",
                        entity.getUsername(),
                        clientRole.getClient(),
                        clientRole.getRole(),
                        clientRole.getClient()
                    );

                    continue;
                }

                RoleModel role = client.getRole(clientRole.getRole());

                if(role==null) {
                    if(this.createClientRoles) {
                        logger.warnf(
                            "Creating missing client role %s.%s for user %s.",
                            clientRole.getClient(),
                            clientRole.getRole(),
                            entity.getUsername()
                        );

                        role = this.roles.addClientRole(client, clientRole.getRole());
                    }
                    else {
                        logger.warnf(
                            "User %s requests client role %s.%s, " +
                            "but client role %s does not exist; " +
                            "client role not assigned.",
                            entity.getUsername(),
                            clientRole.getClient(),
                            clientRole.getRole(),
                            clientRole.getRole()
                        );

                        continue;
                    }
                }

                roleMappings = Stream.concat(roleMappings, Stream.of(role));
            }
        }

        if(!this.importRealmRoles) {
            logger.warnf("Import of realm roles disabled for this provider; skipping.");
	}
	else {
            for (RealmRoleEntity realmRole : entity.getRealmRoles()) {
                String roleId = realmRole.getRole();
                RoleModel role = realm.getRole(roleId);

                if(role==null) {
                    if(this.createRealmRoles) {
                        logger.warnf(
                            "Creating missing realm role %s for user %s.",
                            realmRole.getRole(),
                            entity.getUsername()
                        );

                        role = this.roles.addRealmRole(realm, realmRole.getRole());
                    }
                    else {
                        logger.warnf(
                            "User %s requests realm role %s, " +
                            "but realm role %s does not exist; " +
                            "realm role not assigned.",
                            entity.getUsername(),
                            realmRole.getRole(),
                            realmRole.getRole()
                        );

                        continue;
                    }
                }

                roleMappings = Stream.concat(roleMappings, Stream.of(role));
            }
        }

        return roleMappings;
    }
}
