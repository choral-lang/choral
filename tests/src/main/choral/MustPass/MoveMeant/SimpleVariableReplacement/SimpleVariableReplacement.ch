package choral.amend.simplevariablereplacement;

import choral.channels.SymChannel;

class SimpleVariableReplacement@( A, B ){
    public static void fun( SymChannel@( A, B )<Object> ch_AB ){
        Integer@A IA = 0@A;
        Integer@A I2A = 0@A;
        Integer@B IB = IA + I2A + 1@B;
        Integer@B I2B = IA;

    }
}