class CyclicInheritance_A@W extends CyclicInheritance_B@W{ CyclicInheritance_A(){} }

class CyclicInheritance_B@W extends CyclicInheritance_A@W { CyclicInheritance_B(){} } // expectedError: Cyclic inheritance: 'CyclicInheritance_B@(W)' cannot extend 'CyclicInheritance_A@(W)';
