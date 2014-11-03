#!/bin/sh

MAVEN="mvn -Dmaven.test.skip=true"

RELEASE=sherlok_`date +"%Y%m%d"`
echo "Creating release in $RELEASE"
if [ -e "$RELEASE" ]; then
	echo "Release '$RELEASE' already exists, exiting."; exit;
fi
mkdir "$RELEASE"

# package sherlok module with appassembler
$MAVEN package appassembler:assemble
rc=$?
if [[ $rc != 0 ]] ; then
  echo "ERROR:: could not build sherlok"; exit $rc
fi
mv target/appassembler/* "$RELEASE"/.

# copy config and README
cp README.md "$RELEASE"/.
cp -R public "$RELEASE"/.

# FIXME remove when ok
mkdir "$RELEASE"/local_repo
mkdir "$RELEASE"/local_repo/sherlok
mkdir "$RELEASE"/local_repo/sherlok/sherlok/
mkdir "$RELEASE"/local_repo/sherlok/sherlok/1/
cp sherlok-1.pom "$RELEASE"/local_repo/sherlok/sherlok/1/

chmod 744 "$RELEASE"/bin/sherlok

echo "\n\n************************************************\nDone creating release in '$RELEASE'\n************************************************\n\n"

