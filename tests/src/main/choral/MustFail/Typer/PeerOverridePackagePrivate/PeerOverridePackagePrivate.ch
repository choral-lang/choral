package Typer.PeerOverridePackagePrivate;

import Typer.PeerOverridePackagePrivateBase.Base;

public class PeerOverridePackagePrivate@( A ) //! Implementation is not abstract and does not override abstract method
  extends Base@( A )< String > {
  	// The concrete `process` method cannot override the abstract `process`
  	// method here, because the latter is package-private.
}
