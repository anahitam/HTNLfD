/* Copyright (c) 2009 Charles Rich and Worcester Polytechnic Institute.
 * All Rights Reserved.  Use is subject to license terms.  See the file
 * "license.terms" for information on usage and redistribution of this
 * file and for a DISCLAIMER OF ALL WARRANTIES.
 */
package edu.wpi.disco.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.wpi.cetask.Plan;
import edu.wpi.cetask.Task;
import edu.wpi.cetask.TaskClass;
import edu.wpi.disco.Agenda;
import edu.wpi.disco.Agenda.Plugin;
import edu.wpi.disco.lang.Propose;
import edu.wpi.disco.lang.Reject;

/**
 * Plugin for rejecting the setting of optional inputs.  Typically
 * used in TTSay list.
 */
public class RejectProposeWhatPlugin extends Agenda.Plugin {

   @Override
   public List<Plugin.Item> apply (Plan plan) {
      Task goal = plan.getGoal();
      if ( goal instanceof Propose.What ) {
         Propose.What propose = (Propose.What) goal;
         Plugin.Item item1 = null;
         if ( propose.getGoal() != null && propose.getSlot() != null
               && propose.getValue() == null 
               && propose.getGoal().getType().getProperty(propose.getSlot(), "@optional", false) )
            item1 = new Plugin.Item(new Reject(getDisco(), self(), (Propose.What) goal), plan);
         if ( isApplicable(propose.getGoal(), true) ) {
            Plugin.Item item2 = newItem(propose.getGoal(), plan); 
            if ( item1 == null ) return Collections.singletonList(item2);
            List<Plugin.Item> items = new ArrayList<Plugin.Item>(2);
            items.add(item1); items.add(item2);
            return items;
         }
         if ( item1 != null ) return Collections.singletonList(item1);
      } else if ( isApplicable(goal, false) )
         return Collections.singletonList(newItem(goal, plan));
      return null;
   }
  
   private boolean isApplicable (Task task, boolean asking) {
      TaskClass type = task.getType();
      boolean undefined = false;
      for (String input : type.getDeclaredInputNames())
         if ( !task.isDefinedSlot(input) ) {
            if ( type.getProperty(input, "@optional", false) ) {
               // applicable only if when asking there are at least two undefined optional
               // inputs or one if not asking
               if ( undefined || !asking ) return true; 
               undefined = true;
            } else return false;
         }
      return false;
   }
   
   private Plugin.Item newItem (Task task, Plan plan) {
      return new Plugin.Item(
            new Reject(getDisco(), self(), new Propose.What(getDisco(), null, task, null, null)),
            plan);
   }
   
   public RejectProposeWhatPlugin (Agenda agenda, int priority) { 
      agenda.super(priority);
   }
}
