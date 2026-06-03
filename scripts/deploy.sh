#!/bin/bash
# run by exec-maven-plugin at the end of the "package" phase,
# determines if ../keycloak-developer-deployment is present and, if yes,
# deploys JAR to ../keycloak-developer-deployment/keycloak-custom/providers.

##
# Configuration

name=$(basename "$(readlink -f "$0")")
dir=$(dirname "$(readlink -f "$0")")
developer_deployment_dir=$(readlink -f "$dir"/../../keycloak-developer-deployment)

keycloak_custom_dir="$developer_deployment_dir"/keycloak-custom
jar=$(readlink -f "$dir"/../target/custom-jpa-user-storage.jar)
providers_dir="$keycloak_custom_dir"/providers
sql=$(readlink -f "$dir"/../sql/postgres/userdb.sql)
sql_dir="$developer_deployment_dir"/sql

##
# Main Program
#

echo "INFO: $name starting ..." >&2

if ! [[ -d "$developer_deployment_dir" ]] ; then
	echo "WARNING: Developer deployment not found at $developer_deployment_dir; skipped." >&2
	exit 0
elif ! mkdir -p "$providers_dir" ; then
	echo "ERROR: Creating providers_dir=$providers_dir failed" >&2
	exit 1
elif ! cp -v "$jar" "$providers_dir" ; then
	echo "ERROR: Copying $jar to $providers_dir failed" >&2
	exit 1
elif ! mkdir -p "$sql_dir" ; then
	echo "ERROR: Creating sql_dir=$sql_dir failed" >&2
	exit 1
elif ! cp -v "$sql" "$sql_dir" ; then
	echo "ERROR: Copying $sql to $sql_dir failed" >&2
	exit 1
fi
