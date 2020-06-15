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

printHeader "Downloading Choral"

CHORAL_LATEST_VERSION=$(curl -L -s -H 'Accept: application/json' https://github.com/choral-lang/choral/releases/latest | sed -e 's/.*"tag_name":"\([^"]*\)".*/\1/')
CHORAL_URL="https://github.com/choral-lang/choral/releases/download/${CHORAL_LATEST_VERSION}/choral-${CHORAL_LATEST_VERSION}.zip"
curl -L -o choral.zip $CHORAL_URL
TMP_DIR=$(mktemp -d -t choral-XXXXXXXX)

printHeader "Unpacking Choral"
echo "in $TMP_DIR"

unzip choral.zip -d $TMP_DIR

printHeader "Installing Choral launcher"
echo " in \"$CHORAL_LAUNCHER_DIR\""

mkdir -p $CHORAL_LAUNCHER_DIR
mv $TMP_DIR/choral/launchers/choral $CHORAL_LAUNCHER_DIR/choral
chmod +x $CHORAL_LAUNCHER_DIR/choral

printHeader "Installing Choral libraries"
echo "in \"$CHORAL_HOME_DIR\""
mkdir -p $CHORAL_HOME_DIR
mv $TMP_DIR/choral/dist/* $CHORAL_HOME_DIR/

printHeader "Cleaning up temporary installation files"
echo "in \"$TMP_DIR\""
rm -rf $TMP_DIR

printHeader "Installation Terminated"

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
    -ch, --choral-home              Defines a custom path to install the choral
                                    library binaries
    -y, --assume-yes                Automatic yes to prompts
    -h, --help                      Show this message
EOS
}

printHeader(){
	echo ""
	printf '%*s\n' "${COLUMNS:-$(tput cols)}" '' | tr ' ' =
  printCenter "$1"
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" '' | tr ' ' =
  echo ""
}

printCenter() {
     [[ $# == 0 ]] && return 1

     declare -i TERM_COLS="$(tput cols)"
     declare -i str_len="${#1}"
     [[ $str_len -ge $TERM_COLS ]] && {
          echo "$1";
          return 0;
     }

     declare -i filler_len="$(( (TERM_COLS - str_len) / 2 ))"
     [[ $# -ge 2 ]] && ch="${2:0:1}" || ch=" "
     filler=""
     for (( i = 0; i < filler_len; i++ )); do
          filler="${filler}${ch}"
     done

     printf "%s%s%s" "$filler" "$1" "$filler"
     [[ $(( (TERM_COLS - str_len) % 2 )) -ne 0 ]] && printf "%s" "${ch}"
     printf "\n"
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
    -ch|--choral-home)
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
