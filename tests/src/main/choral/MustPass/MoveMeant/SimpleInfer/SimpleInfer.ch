package choral.MustPass.MoveMeant.SimpleInfer;

import choral.channels.SymChannel;

class SimpleInfer@(A, B) {
  SymChannel@(A,B)<Integer> ch;
  Integer@B main(Integer@A x, Integer@B y) {
    return x + y;
  }
}