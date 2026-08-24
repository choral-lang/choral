package Misc.CyclicInheritance;

class CyclicInheritance@W extends CyclicInheritance_B@W{ CyclicInheritance(){} }

class CyclicInheritance_B@W extends CyclicInheritance@W { CyclicInheritance_B(){} } //! Cyclic inheritance
