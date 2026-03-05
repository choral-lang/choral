package choral.MustPass.Typer.InterfaceDefaultMethod;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "InterfaceDefaultMethod" )
public interface InterfaceDefaultMethod {
	default void defaultMehod();
}
