#!/bin/bash
# Tests for custom-jpa-user-storage
# shellcheck disable=SC1091,SC2154

##
# Configuration

dir=$(readlink -f "$(dirname "$0")")
. "$dir"/include.sh

component_name="userdb"
component_provider_id="custom-jpa-user-storage"
component_provider_type="org.keycloak.storage.UserStorageProvider"
test_username="mmustermann"
test_attribute="phoneNumber"
test_value="0123 456789"

##
# Main Program

userdb_id=$(
    kcadm get components | \
        jq -r '.[] | select(.name=="userdb").id'
)

if [[ -n $userdb_id ]] ; then
    echo "INFO: Component \"userdb\" already present with id=$userdb_id; not creating." >&2
else
    kcadm create components \
            --set name="$component_name" \
            --set providerId="$component_provider_id" \
            --set providerType="$component_provider_type"

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
        jq -r '.[] | select(.username=="'"$test_username"'").username'
)

if check "${PIPESTATUS[@]}" ; then
    echo "INFO: Searched for username==\"$test_username\"." >&2
else
    echo "ERROR: Searching for username==\"$test_username\" failed." >&2
    exit 1
fi

if [[ -n $username ]] ; then
    echo "INFO: Found expected sample user with username==\"$test_username\"." >&2
else
    echo "ERROR: Expected sample user with username==\"$test_username\" not found." >&2
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
        --fields 'username,attributes('"$test_attribute"')' | \
        jq -r '.[] | select(.username=="'"$test_username"'").attributes.'"$test_attribute"'[0]'
)

if check "${PIPESTATUS[@]}" ; then
   echo "INFO: Queried user \"$test_username\" in Keycloak realm \"$keycloak_realm\" for value of attribute \"$test_attribute\"." >&2
else
   echo "ERROR: Could not query user \"$test_username\" in Keycloak realm \"$keycloak_realm\" for value of attribute \"$test_attribute\"." >&2
   exit 1
fi

if [[ $value = "$test_value" ]] ; then
    echo "INFO: Profile attribute \"$test_attribute\" of user \"$test_username\" in Keycloak realm \"$keycloak_realm\" has expected value \"$test_value\"." >&2
else
    echo "ERROR: Profile attribute \"$test_attribute\" of user \"$test_username\" in Keycloak realm \"$keycloak_realm\" does not have expected value \"$test_value\"." >&2
    exit 1
fi
