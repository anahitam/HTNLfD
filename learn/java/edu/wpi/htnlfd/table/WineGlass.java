package edu.wpi.htnlfd.table;

public class WineGlass extends Glass {

   public WineGlass (String name, Location location) {
      super(name, location);
   }
 
   public static final WineGlass WG1 = new WineGlass("WG1", null);
}
