package choral.MustPass.MoveMeant.VitalsStreaming.utils;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Sensor" )
public class Sensor {
	public Sensor() {
		
	}

	public Boolean isOn() {
		return true;
	}
	
	public VitalsMsg next() {
		return null;
	}

}
