package edu.wpi.htnlfd.tireRotation;

import java.io.PrintStream;

import edu.wpi.htnlfd.domain.*;

public class Tire extends PhysObj {

	public Tire(String name, Location location) {
		super(name, location);
	}

	public void print(PrintStream stream, String indent) {
		stream.append(this.name + this.getLocation() + "\n");
	}

	public String find() {
		String[] wheelName = this.name.split("_");
		return "$world.MyCar." + wheelName[0].substring(0, 2) +"wheel.getTire()";
	}
}
