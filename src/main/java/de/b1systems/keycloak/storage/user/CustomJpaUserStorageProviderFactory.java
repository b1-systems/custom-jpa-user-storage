/* Copyright 2024-2026  B1 Systems GmbH <info@b1-systems.de>
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

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import java.util.ArrayList;
import java.util.List;

public class CustomJpaUserStorageProviderFactory
implements UserStorageProviderFactory<CustomJpaUserStorageProvider>
{
    public static final String PROVIDER_ID = "custom-jpa-user-storage";

    @Override
    public String getHelpText() {
        return "Custom JPA User Storage";
    }

    private static final Logger logger = Logger
        .getLogger(CustomJpaUserStorageProviderFactory.class);
    private static List<ProviderConfigProperty> configProperties =
        new ArrayList<ProviderConfigProperty>();

    @Override
    public CustomJpaUserStorageProvider create(
        KeycloakSession session,
        ComponentModel model
    ) {

        return new CustomJpaUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void close() {
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return
            ProviderConfigurationBuilder.create()
              .property()
                .name("readOnly")
                .label("Read-only")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(true)
                .helpText(
                    "If set to ON, this provider is read-only; " +
                    "users can not be added or deleted, and no user properties " +
                    "or attributes can be modified."
                )
                .add()
              .property()
                .name("importRealmRoles")
                .label("Import realm roles")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .helpText(
                    "If set to ON, this provider will attempt to import " +
                    "realm role mappings of users from the database."
                )
                .add()
              .property()
                .name("createRealmRoles")
                .label("Create missing realm roles")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .helpText(
                    "If set to ON, this provider will create missing realm roles " +
                    "required by user realm role mappings from this datasource. "  +
                    "If set to OFF, a missing realm role required by a user " +
                    "role mapping will emit a warning log message, but the user " +
                    "will be imported anyway."
                )
                .add()
              .property()
                .name("importClientRoles")
                .label("Import client roles")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .helpText(
                    "If set to ON, this provider will attempt to import " +
                    "client role mappings of users from the database. " +
                    "If import of a user requires a client role mapping " +
                    "for a client that does not exist, a warning log message " +
                    "will be emitted, but the user will be imported anyway."
                )
                .add()
              .property()
                .name("createClientRoles")
                .label("Create missing client roles")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .helpText(
                    "If set to ON, this provider will create missing client roles " +
                    "required by user client role mappings from this datasource. "  +
                    "If set to OFF, a missing client role required by a user " +
                    "role mapping will emit a warning log message, but the user " +
                    "will be imported anyway."
                )
                .add()
              .build();
    }
}
