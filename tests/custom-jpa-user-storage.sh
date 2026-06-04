#!/bin/bash
# Using Keycloak REST-API, instanciate custom JPA provider if missing,
# and check if expected example user was successfully imported.

##
# Configuration

keycloak_url="http://keycloak:8080"
keycloak_realm="master"
keycloak_admin_user="admin"
keycloak_admin_password="admin"
keycloak_component_name="userdb"
keycloak_component_provider_id="custom-jpa-user-storage"
keycloak_component_provider_type="org.keycloak.storage.UserStorageProvider"
keycloak_test_username="mmustermann"
keycloak_test_attribute="phoneNumber"
keycloak_test_value="0123 456789"

dir=$(readlink -f "$(dirname "$0")")

##
# Functions

check() {
    for status in "$@" ; do
        [[ $status -ne 0 ]] && {
            return 1
        }
    done

    return 0
}

kcadm_cmdline=(
    /opt/keycloak/bin/kcadm.sh
)

kcadm() {
    "${kcadm_cmdline[@]}" "$@"
}

##
# Main Program

kcadm config credentials \
    --server "$keycloak_url" \
    --realm "$keycloak_realm" \
    --user "$keycloak_admin_user" \
    --password "$keycloak_admin_password"

if check "${PIPESTATUS[@]}" ; then
    echo "INFO: Authenticated to Keycloak REST-API." >&2
else
    echo "ERROR: Authenticating to Keycloak REST-API failed." >&2
    exit 1
fi

userdb_id=$(
    kcadm get components | \
        jq -r '.[] | select(.name=="userdb").id'
)

if [[ -n $userdb_id ]] ; then
    echo "INFO: Component \"userdb\" already present with id=$userdb_id; not creating." >&2
else
    kcadm create components \
            --set name="$keycloak_component_name" \
            --set providerId="$keycloak_component_provider_id" \
            --set providerType="$keycloak_component_provider_type"

    if check "${PIPESTATUS[@]}" ; then
        echo "INFO: Created userdb component." >&2
    else
        echo "ERROR: Creating userdb component failed." >&2
        exit 1
    fi
fi

username=$(
    kcadm get users \
        --fields username | \
        jq -r '.[] | select(.username=="'"$keycloak_test_username"'").username'
)

if check "${PIPESTATUS[@]}" ; then
    echo "INFO: Searched for username==\"$keycloak_test_username\"." >&2
else
    echo "ERROR: Searching for username==\"$keycloak_test_username\" failed." >&2
    exit 1
fi

if [[ -n $username ]] ; then
    echo "INFO: Found expected sample user with username==\"$keycloak_test_username\"." >&2
else
    echo "ERROR: Expected sample user with username==\"$keycloak_test_username\" not found." >&2
    exit 1
fi

attribute=$(
    kcadm get realms/master/users/profile | \
        jq -r '.attributes[] | select(.name=="phoneNumber").name'
)

if check "${PIPESTATUS[@]}" ; then
   echo "INFO: Got user profile of Keycloak realm \"$keycloak_realm\"." >&2
else
   echo "ERROR: Could not get user profile of Keycloak realm \"$keycloak_realm\"." >&2
   exit 1
fi

if [[ -n $attribute ]] ; then
    echo "INFO: User profile attribute \"phoneNumber\" already present; skipping creation." >&2
else
    kcadm update realms/"$keycloak_realm"/users/profile \
        -b "$(cat "$dir"/custom-jpa-user-storage/userprofile.js)"

    if check "${PIPESTATUS[@]}" ; then
        echo "INFO: Updated declarative user profile in Keycloak realm \"$keycloak_realm\"." >&2
    else
        echo "ERROR: Updating declarative user profile in Keycloak realm \"$keycloak_realm\" failed." >&2
        exit 1
    fi
fi

value=$(
    kcadm get users \
        --fields 'username,attributes('"$keycloak_test_attribute"')' | \
        jq -r '.[] | select(.username=="'"$keycloak_test_username"'").attributes.'"$keycloak_test_attribute"'[0]'
)

if check "${PIPESTATUS[@]}" ; then
   echo "INFO: Queried user \"$keycloak_test_username\" in Keycloak realm \"$keycloak_realm\" for value of attribute \"$keycloak_test_attribute\"." >&2
else
   echo "ERROR: Could not query user \"$keycloak_test_username\" in Keycloak realm \"$keycloak_realm\" for value of attribute \"$keycloak_test_attribute\"." >&2
   exit 1
fi

if [[ $value = "$keycloak_test_value" ]] ; then
    echo "INFO: Profile attribute \"$keycloak_test_attribute\" of user \"$keycloak_test_username\" in Keycloak realm \"$keycloak_realm\" has expected value \"$keycloak_test_value\"." >&2
else
    echo "ERROR: Profile attribute \"$keycloak_test_attribute\" of user \"$keycloak_test_username\" in Keycloak realm \"$keycloak_realm\" does not have expected value \"$keycloak_test_value\"." >&2
    exit 1
fi
