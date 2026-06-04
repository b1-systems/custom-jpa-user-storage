#!/bin/bash
# run by exec-maven-plugin at the end of the "package" phase,
# determines if ../keycloak-developer-deployment is present and, if yes,
# deploys JAR to ../keycloak-developer-deployment/keycloak-custom/providers.

##
# Configuration

artifact_id="custom-jpa-user-storage"

name=$(basename "$(readlink -f "$0")")
dir=$(dirname "$(readlink -f "$0")")

conf_example=$(readlink -f "$dir"/../conf/keycloak.conf.example)
jar=$(readlink -f "$dir"/../target/"$artifact_id".jar)
sql=$(readlink -f "$dir"/../sql/postgres/"$artifact_id".sql)

developer_deployment_dir=$(readlink -f "$dir"/../../keycloak-developer-deployment)
keycloak_custom_dir="$developer_deployment_dir"/keycloak-custom
sql_dir="$developer_deployment_dir"/sql
providers_dir="$keycloak_custom_dir"/providers
conf_d_dir="$keycloak_custom_dir"/conf/keycloak.conf.d
conf="$conf_d_dir"/"$artifact_id".conf

##
# Main Program
#

echo "INFO: $name starting ..." >&2

if ! [[ -d "$developer_deployment_dir" ]] ; then
	echo "WARNING: Developer deployment not found at $developer_deployment_dir; skipped." >&2
	exit 0
fi

if ! [[ -e "$conf_example" ]] ; then
    echo "INFO: Found no configuration example to deploy; skipped." >&2
elif ! mkdir -p "$conf_d_dir" ; then
    echo "ERROR: Creating conf_d_dir=$conf_d_dir failed" >&2
    exit 1
elif ! cp -v "$conf_example" "$conf" ; then
    echo "ERROR: Copying $conf_example to $conf failed" >&2
    exit 1
fi

if ! [[ -e "$jar" ]] ; then
    echo "INFO: Found no JAR to deploy; skipped." >&2
elif ! mkdir -p "$providers_dir" ; then
	echo "ERROR: Creating providers_dir=$providers_dir failed" >&2
	exit 1
elif ! cp -v "$jar" "$providers_dir" ; then
	echo "ERROR: Copying $jar to $providers_dir failed" >&2
	exit 1
fi

if ! [[ -e "$sql" ]] ; then
    echo "INFO: Found no SQL to deploy; skipped." >&2
elif ! mkdir -p "$sql_dir" ; then
	echo "ERROR: Creating sql_dir=$sql_dir failed" >&2
	exit 1
elif ! cp -v "$sql" "$sql_dir" ; then
	echo "ERROR: Copying $sql to $sql_dir failed" >&2
	exit 1
fi
