#!/bin/sh

BIN_DIR=`realpath $1`
ln -sf $PWD/scripts/choral $BIN_DIR/

DIST_DIR=$1/choral-dist

ln -sf $PWD/dist/target/choral-standalone.jar $DIST_DIR/