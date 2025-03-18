#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvskanmothelse/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export skanmothelse_SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvskanmothelse/username)
fi

if test -f /var/run/secrets/nais.io/srvskanmothelse/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export skanmothelse_SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvskanmothelse/password)
fi

if test -f /var/run/secrets/nais.io/srvjiradokdistavstemming/username;
then
    echo "Setting SKANMOTHELSE_JIRA_USERNAME"
    export SKANMOTHELSE_JIRA_USERNAME=$(cat /var/run/secrets/nais.io/srvjiradokdistavstemming/username)
fi

if test -f /var/run/secrets/nais.io/srvjiradokdistavstemming/password;
then
    echo "Setting SKANMOTHELSE_JIRA_PASSWORD"
    export SKANMOTHELSE_JIRA_PASSWORD=$(cat /var/run/secrets/nais.io/srvjiradokdistavstemming/password)
fi