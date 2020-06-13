#!/bin/bash

CHORAL_LAUNCHER_DIR="/usr/local/bin"
CHORAL_HOME_DIR="/usr/local/lib/choral"
ASSUME_YES=false
HELP=false
POSITIONAL=()

main() {

parseCommands "$@"

if [[ $HELP = true ]] ; then
  help
  exit 0
fi

cat <<EOS
The Choral installer is going to install the following files:
  - the choral launchers inside folder "$CHORAL_LAUNCHER_DIR"
  - the choral binary libraries inside folder "$CHORAL_HOME_DIR"

EOS

PROCEED="y"
if [[ $ASSUME_YES = false ]] ; then
  read -p 'Please, confirm that those folders are correct before proceeding with the installation (y/Any): ' PROCEED
  echo ""
fi

case "$PROCEED" in
 [yY])
        ;;
    *)
        echo "Installation interrupted by the user"
        help
        exit 0
        ;;
esac

println "===== Downloading Choral ====="

CHORAL_LATEST_VERSION=$(curl -L -s -H 'Accept: application/json' https://github.com/choral-lang/choral/releases/latest | sed -e 's/.*"tag_name":"\([^"]*\)".*/\1/')
CHORAL_URL="https://github.com/choral-lang/choral/releases/download/${CHORAL_LATEST_VERSION}/choral-${CHORAL_LATEST_VERSION}.zip"
curl -L -o choral.zip $CHORAL_URL
TMP_DIR=$(mktemp -d -t choral-XXXXXXXX)

println "===== Unpacking Choral =====\n     in $TMP_DIR"
unzip choral.zip -d $TMP_DIR

println "===== Installing Choral launcher in =====\n     \"$CHORAL_LAUNCHER_DIR\""
mkdir -p $CHORAL_LAUNCHER_DIR
mv $TMP_DIR/choral/launchers/choral $CHORAL_LAUNCHER_DIR/choral
chmod +x $CHORAL_LAUNCHER_DIR/choral

println "===== Installing Choral libraries in =====\n     \"$CHORAL_HOME_DIR\""
mkdir -p $CHORAL_HOME_DIR
mv $TMP_DIR/choral/dist/* $CHORAL_HOME_DIR/

println "===== Cleaning up temporary installation files in =====\n     \"$TMP_DIR\""
rm -rf $TMP_DIR

println "** Installation Terminated"

cat <<EOS

To make sure Choral is propertly installed, please check that:
 - the installation folder of the Choral launcher "$CHORAL_LAUNCHER_DIR" is in your in PATH variable;
 - the CHORAL_HOME variable points to the folder containing the Choral libraries, i.e., CHORAL_HOME="$CHORAL_HOME_DIR". In some OSes, you can achieve this by having export CHORAL_HOME="$CHORAL_HOME_DIR" in ~/.bashrc, ~/.bash_profile, or /etc/profile.
EOS

}

## Auxiliary functions

help() {
cat <<EOS

Install script for the Choral language.
The additional options below may be appended to the launch command.

    -l, --launchers                 Defines a custom path to install the choral 
                                    launchers
    -b, --binaries                  Defines a custom path to install the choral
                                    library binaries
    -y, --assume-yes                Automatic yes to prompts
    -h, --help                      Show this message
EOS
}

println(){
  printf "$1\n\n"
}

parseCommands() {
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -l|--launchers)
    CHORAL_LAUNCHER_DIR="$2"
    shift
    shift
    ;;
    -b|--binaries)
    CHORAL_HOME_DIR="$2"
    shift
    shift
    ;;
    -y|--yes|--assume-yes)
    ASSUME_YES=true
    shift
    ;;
    -h|--help)
    HELP=true
    shift
    ;;
    *)
    POSITIONAL+=("$1")
    shift
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters
}

main "$@"; exit