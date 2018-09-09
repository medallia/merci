#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  ./gradlew publish devSnapshot --stacktrace -Dorg.ajoberstar.grgit.auth.session.config.StrictHostKeyChecking=no -PsonatypeUsername=${sonatypeUsername} -PsonatypePassword=${sonatypePassword} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=merci.travis.gpg
fi

