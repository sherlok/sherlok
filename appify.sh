#!/bin/sh
#
# Copyright (C) 2014-2015 Renaud Richardet (renaud@apache.org)
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
#####################################
#   Bash script to create a         #
#   standalone Sherlok application  #
#   from one pipeline               #
#####################################

MAVEN="mvn -Dmaven.test.skip=true"
#MAVEN="mvn clean test"
SHERLOK_VERSION=0.1-SNAPSHOT


if [ -z "$1" ]
  then
    echo "No pipeline id supplied. Format: pipeline_name:pipeline_id"; exit;
fi
PIPELINE=$1


# create app directory
RELEASE=sherlok_app_`date +"%Y%m%d"`
echo "Creating app in $RELEASE\n"
if [ -e "$RELEASE" ]; then
    echo "App '$RELEASE' already exists, exiting."; exit;
fi
mkdir "$RELEASE"


echo '\n\nINSTALL SHERLOK LOCALLY\n----------------------\n'
$MAVEN clean install
rc=$?
if [[ $rc != 0 ]] ; then
  echo "ERROR:: could not install sherlok locally"; exit $rc
fi


echo '\n\nGENERATE APP POM\n----------------------\n'
$MAVEN exec:java -Dexec.mainClass="org.sherlok.Appify" \
 -Dexec.classpathScope=runtime -Dexec.args="$PIPELINE $SHERLOK_VERSION"
rc=$?
if [[ $rc != 0 ]] ; then
  echo "ERROR:: could not build pom"; exit $rc
fi


echo '\n\nPACKAGE APP WITH APPASSEMBLER\n----------------------\n'
$MAVEN package appassembler:assemble -f runtime/pipelines/$PIPELINE.pom.xml
rc=$?
if [[ $rc != 0 ]] ; then
  echo "ERROR:: could not build app"; exit $rc
fi
mv runtime/pipelines/target/appassembler/* "$RELEASE"/.
rm -rf runtime/pipelines/target

# copy config, public and README
cp -R config "$RELEASE"/.
cp -R public "$RELEASE"/.

# add git revision
git rev-parse HEAD > "$RELEASE/git_revision.txt"

chmod 744 "$RELEASE"/bin/sherlok_app

echo "\n\n************************************************"
echo "Done creating $PIPELINE app in '$RELEASE'"
echo "************************************************\n\n"
