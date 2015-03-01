#!/bin/sh
#
# Copyright (C) 2014 Renaud Richardet (renaud@apache.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#################################
#   Bash script to create a     #
#   standalone Sherlok release  #
#################################

MAVEN="mvn -Dmaven.test.skip=true"
#MAVEN="mvn clean test"

# create release directory
RELEASE=sherlok_`date +"%Y%m%d"`
echo "Creating release in $RELEASE\n"
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

# copy config, public and README
cp -R config "$RELEASE"/.
cp -R public "$RELEASE"/.
cp README.md "$RELEASE"/.
# cleanup caches
rm -rf "$RELEASE/config/ruta/.engines/*"
rm -rf "$RELEASE/config/ruta/.pipelines/*"

# add git revision
git rev-parse HEAD > "$RELEASE/git_revision.txt"

chmod 744 "$RELEASE"/bin/sherlok

echo "\n\n************************************************\nDone creating release in '$RELEASE'\n************************************************\n\n"

