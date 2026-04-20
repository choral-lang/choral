package choral.MustFail.Typer.PeerOverridePackagePrivate;

import choral.MustFail.Typer.PeerOverridePackagePrivateBase.Base;

public class PeerOverridePackagePrivate@( A )
  extends Base@( A )< String > { //! Implementation is not abstract and does not override abstract method
  	// The concrete `process` method cannot override the abstract `process`
  	// method here, because the latter is package-private.
}
