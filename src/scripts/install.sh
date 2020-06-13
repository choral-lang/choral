#!/bin/sh

# Preamble
## String formatters
if [[ -t 1 ]]; then
  tty_escape() { printf "\033[%sm" "$1"; }
else
  tty_escape() { :; }
fi
tty_mkbold() { tty_escape "1;$1"; }
tty_underline="$(tty_escape "4;39")"
tty_blue="$(tty_mkbold 34)"
tty_red="$(tty_mkbold 31)"
tty_bold="$(tty_mkbold 39)"
tty_reset="$(tty_escape 0)"

echoFancy() {
	echo "$1${tty_reset}"
}

showCommands() {
cat <<EOS
To launch the Choral installer with different parameters you can run:
 curl https://raw.githubusercontent.com/choral-lang/choral/master/src/scripts/install.sh | bash -s /path/to/store/the/launcher /path/to/store/the/binaries

EOS
}

# Parameters

CHORAL_LAUNCHER_DIR="${1:-"/usr/local/bin"}"
CHORAL_HOME="${2:-"/usr/local/lib/choral"}"

cat <<EOS
The Choral installer is going to install the following files:
  - the choral launchers inside folder "$CHORAL_LAUNCHER_DIR"
  - the choral libraries inside folder "$CHORAL_HOME"

EOS

read -p 'Please, confirm that those folders are correct before proceeding with the installation (y/Any): ' proceed
echo ""

case "$proceed" in 
 [yY])
        ;;
    *)
        echoFancy "${tty_bold}Installation interrupted by the user"
        showCommands
        exit 0
        ;;
esac


# Main

echoFancy "${tty_bold}Getting the latest Choral version identifier from GitHub"

CHORAL_LATEST_VERSION=$(curl -L -s -H 'Accept: application/json' https://github.com/choral-lang/choral/releases/latest | sed -e 's/.*"tag_name":"v\([^"]*\)".*/\1/')
echoFancy "${tty_bold}Latest version is $CHORAL_LATEST_VERSION"

TMP_DIR=$(mktemp -d -t choral-XXXXXXXX)
CHORAL_URL="https://github.com/choral-lang/choral/releases/download/v${CHORAL_LATEST_VERSION}/choral-${CHORAL_LATEST_VERSION}.zip"
echoFancy "${tty_bold}Downloading Choral from $CHORAL_URL into $TMP_DIR"
curl -L -o $TMP_DIR/choral.zip $CHORAL_URL

echoFancy $tty_bold "Unpacking Choral in $TMP_DIR"
unzip $TMP_DIR/choral.zip -d $TMP_DIR

echoFancy "${tty_bold}Installing Choral launcher in $CHORAL_LAUNCHER_DIR"
mkdir -p $CHORAL_LAUNCHER_DIR
mv $TMP_DIR/choral/launchers/choral $CHORAL_LAUNCHER_DIR/choral
chmod +x $CHORAL_LAUNCHER_DIR/choral
echoFancy "${tty_bold}Installing Choral libraries in $CHORAL_HOME"
mkdir -p $CHORAL_HOME
mv $TMP_DIR/choral/dist/* $CHORAL_HOME/

echoFancy "${tty_bold}Cleaning up temporary installation files in $TMP_DIR"
rm -rf $TMP_DIR

echo ""
echoFancy "${tty_bold}Installation Terminated"

cat <<EOS

To make sure Choral is propertly installed, please check that:
 - the installation folder of the Choral launcher "$CHORAL_LAUNCHER_DIR" is in your in PATH variable;
 - the CHORAL_HOME variable points to the folder containing the Choral libraries, i.e., CHORAL_HOME="$CHORAL_HOME". In some OSes, you can achieve this by having export CHORAL_HOME="$CHORAL_HOME" in ~/.bashrc, ~/.bash_profile, or /etc/profile.
EOS
