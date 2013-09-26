package edu.wpi.htnlfd;

import edu.wpi.cetask.*;
import edu.wpi.cetask.DecompositionClass.Binding;
import edu.wpi.disco.*;
import edu.wpi.disco.lang.Utterance;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.script.*;

public class Demonstration {

   private DocumentBuilderFactory factory;

   private DocumentBuilder builder;

   private Document document;

   TaskModel taskModel = null;

   private final String ReferenceFrame = "referenceFrame";

   private final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

   private final String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";

   private final String namespacePrefix;

   private ArrayList<String[]> OrderedTasks = new ArrayList<String[]>();

   private TaskClass recipeTaskClass = null;

   class TempClass {
      String name;

      ArrayList<Task> steps = new ArrayList<Task>();

      ArrayList<String> inputs = new ArrayList<String>();
      
      ArrayList<String> stepNames = new ArrayList<String>();

      public TempClass (String name, Task step, String input, String stepStrValue) {

         this.name = name;
         this.steps.add(step);
         this.inputs.add(input);
         this.stepNames.add(stepStrValue);
      }

   }

   public Demonstration () {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      String[] dNSNameArray = namespace.split(":");
      namespacePrefix = dNSNameArray[dNSNameArray.length - 1];
      try {
         builder = factory.newDocumentBuilder();
         // document = builder.newDocument();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(
               "An error occured while creating the document builder", e);
      }
   }

   public List<Task> findDemonstration (Disco disco, String taskName) {
      List<Task> demonstratedTasks = new ArrayList<Task>();
      List<Task> demonstratedTasksReversed = new ArrayList<Task>();
      Object parent = (disco.getStack().get(0).getChildren().get(disco
            .getStack().get(0).getChildren().size() - 1));
      if ( parent instanceof Segment ) {

         for (int i = ((Segment) parent).getChildren().size() - 1; i >= 0; i--) {
            Object child = ((Segment) parent).getChildren().get(i);
            if ( (child instanceof Task) ) {
               Task task = (Task) child;
               if ( !(task instanceof Utterance) )
                  ;// demonstratedTasks.add(task);
            } else if ( child instanceof Segment ) {
               Segment segment = (Segment) child;
               demonstratedTasks.add(segment.getPurpose());
            }
         }

         for (int i = demonstratedTasks.size() - 1; i >= 0; i--) {
            Task myTask = demonstratedTasks.get(i);
            demonstratedTasksReversed.add(myTask);
         }
         return demonstratedTasksReversed;
      } else {
         return null;
      }
   }

   public void writeDOM (Disco disco, String fileName, String taskName,
         List<Task> steps, String input) throws Exception {

      // Writing document into xml file
      document = builder.newDocument();
      DOMSource domSource = new DOMSource(document);
      File demonstrationFile = new File(fileName);
      if ( !demonstrationFile.exists() )
         demonstrationFile.createNewFile();

      try (FileOutputStream fileOutputStream = new FileOutputStream(
            demonstrationFile, false)) {

         StreamResult streamResult = new StreamResult(fileOutputStream);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();

         buildDOM(disco, taskName, steps, input);

         // Adding indentation and omitting xml declaration
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(
               "{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.transform(domSource, streamResult);

      } catch (Exception e) {

         throw e;
      }

   }

   public void readDOM (Disco disco, String fileName) {
      taskModel = disco.getInteraction().load(fileName);
   }

   private void buildDOM (Disco disco, String taskName, List<Task> steps,
         String input) throws NoSuchMethodException, ScriptException {

      Element taskModelElement = null;

      taskModelElement = document.createElementNS(xmlnsValue, "taskModel");
      document.appendChild(taskModelElement);

      Attr about = document.createAttribute("about");
      about.setValue(namespace);
      taskModelElement.setAttributeNode(about);
      Attr xmlns = document.createAttribute("xmlns");
      xmlns.setValue(xmlnsValue);
      taskModelElement.setAttributeNode(xmlns);
      Element recipe = null;

      List<String> namespaces = new ArrayList<String>();

      if ( this.taskModel != null ) {
         recipe = learnedTaskmodelToDom(namespaces, taskName, steps,
               taskModelElement, input);
      }
      Element taskElement = null;

      if ( recipe == null ) {
         taskElement = document.createElementNS(xmlnsValue, "task");
         taskModelElement.appendChild(taskElement);
         Attr idTask = document.createAttribute("id");
         idTask.setValue(taskName);
         taskElement.setAttributeNode(idTask);
      } else {
         taskElement = recipe;

         Element inputTask = document.createElementNS(xmlnsValue, "input");
         taskElement.insertBefore(inputTask, taskElement.getFirstChild());
         Attr inputName = document.createAttribute("name");
         inputName.setValue(input);
         inputTask.setAttributeNode(inputName);
         Attr inputType = document.createAttribute("type");
         inputType.setValue("boolean");
         inputTask.setAttributeNode(inputType);

      }

      demonstratedTaskToDom(disco, taskElement, taskName, steps, namespaces,
            recipe, input);

      for (String namespaceOfTasks : namespaces) {

         String[] namespaceOfTaskArray = namespaceOfTasks.split(":");
         String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

         Attr xmlnsReference = document.createAttribute("xmlns:"
            + namespaceOfTask);
         xmlnsReference.setValue(namespaceOfTasks);
         taskModelElement.setAttributeNode(xmlnsReference);
      }

   }

   public void demonstratedTaskToDom (Disco disco, Element taskElement,
         String taskName, List<Task> steps, List<String> namespaces,
         Element recipe, String input) throws NoSuchMethodException,
         ScriptException {

      Element subtasks = document.createElementNS(xmlnsValue, "subtasks");
      taskElement.appendChild(subtasks);
      Attr idSubtask = document.createAttribute("id");
      int countSubtask = 0;
      NodeList nodes = taskElement.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
         if ( nodes.item(i).getNodeName().equals("subtasks") )
            countSubtask++;
      }
      subtasks.setAttributeNode(idSubtask);
      countSubtask = taskElement.getChildNodes().getLength() == 1 ? 1
         : countSubtask + 1;
      idSubtask.setValue(Character.toLowerCase(taskName.charAt(0))
         + (taskName.length() > 1 ? taskName.substring(1) : "") + countSubtask);
      Map<String, Integer> StepNames = new HashMap<String, Integer>();
      Map<String, String> bindingsInputs = new HashMap<String, String>();
      Map<String, String> bindingsOutputs = new HashMap<String, String>();

      ArrayList<TempClass> inputs = new ArrayList<TempClass>();

      List<String> inputsNumbers = new ArrayList<String>();
      Map<String, Task> outputs = new HashMap<String, Task>();
      List<String> outputNumbers = new ArrayList<String>();
      for (Task step : steps) {

         String stepName = step.getType().getId();
         int count = 1;
         Map.Entry<String, Integer> stepEntry = null;
         for (Map.Entry<String, Integer> entry : StepNames.entrySet()) {
            if ( stepName.equals(entry.getKey()) ) {
               entry.setValue(entry.getValue() + 1);
               stepEntry = entry;
               count = stepEntry.getValue();
               break; // breaking because its one to one map
            }
         }

         if ( stepEntry == null ) {
            StepNames.put(stepName, count);
         }

         if ( !namespaces.contains(step.getType().getNamespace())
            && !step.getType().getNamespace().equals(namespace) ) {
            namespaces.add(step.getType().getNamespace());
         }

         Element subtaskStep = document.createElementNS(xmlnsValue, "step");
         subtasks.appendChild(subtaskStep);
         Attr nameSubtaskStep = document.createAttribute("name");

         String stepStrValue = Character.toLowerCase(stepName.charAt(0))
            + (stepName.length() > 1 ? stepName.substring(1) : "") + count;
         nameSubtaskStep.setValue(stepStrValue);
         subtaskStep.setAttributeNode(nameSubtaskStep);
         Attr valueSubtaskStep = document.createAttribute("task");
         String namespaceName = step.getType().getNamespace();

         String[] namespaceOfTaskArray = namespaceName.split(":");
         String namespaceOfTask = namespaceOfTaskArray[namespaceOfTaskArray.length - 1];

         if ( namespacePrefix.compareTo(namespaceOfTask) != 0 )
            valueSubtaskStep.setValue(namespaceOfTask + ":" + stepName);
         else
            valueSubtaskStep.setValue(stepName);
         subtaskStep.setAttributeNode(valueSubtaskStep);

         // //////////////////////////////////////////////////

         // ///////////////////////////////////////////////

         for (String inputName : step.getType().getDeclaredInputNames()) {

            String bindingSlotvalue = "$" + stepStrValue + "." + inputName;

            // String temp2 = step.getSlotValueToString(inputName);

            Object inputBinding = (((Invocable) disco.getScriptEngine())
                  .invokeFunction("find", step.getSlotValue(inputName)));

            String inputBindingValue = (String) inputBinding;
            TempClass findInput = null;
            for (TempClass in : inputs) {
               if ( in.name.equals(inputBindingValue) ) {
                  findInput = in;
                  break;
               }
            }
            if ( findInput != null ) {
               boolean contain = false;
               for (String str : findInput.inputs) {
                  if ( str.contains(inputName) ) {
                     contain = true;
                     break;
                  }
               }
               if ( !contain ) {

                  findInput.inputs.add(inputName);
                  findInput.steps.add(step);
                  findInput.stepNames.add(stepStrValue);
                  // System.out.println(inputBindingValue + " " + nameType[0]);
               }

            } else {

               inputs.add(new TempClass(inputBindingValue, step, inputName, stepStrValue));
               // System.out.println(inputBindingValue + " " + nameType[0]);
            }

            bindingsInputs.put(bindingSlotvalue, inputBindingValue);
         }

         for (String outputName : step.getType().getDeclaredOutputNames()) {

            String bindingSlot = "$" + stepStrValue + "." + outputName;
            boolean contain = false;
            String bindingSlotValue = null;
            for (int i = outputNumbers.size() - 1; i >= 0; i--) {
               if ( outputNumbers.get(i).contains(outputName) ) {
                  int start = outputNumbers.get(i).lastIndexOf(outputName)
                     + outputName.length();
                  int end = outputNumbers.get(i).length();
                  String number = outputNumbers.get(i).substring(start, end);
                  int num = Integer.parseInt(number);
                  num++;
                  outputNumbers.add(outputName + num);
                  bindingSlotValue = outputName + num;
                  contain = true;
                  break;

               }
            }
            if ( !contain ) {
               outputNumbers.add(outputName + "1");
               bindingSlotValue = outputName + "1";
            }
            outputs.put(bindingSlotValue, step);
            bindingsOutputs.put("$this." + bindingSlotValue, bindingSlot);
         }

      }

      if ( recipe != null ) {
         addRecipe(taskElement, input, subtasks, inputs, recipe);
      }

      // taskElement.appendChild();
      if ( recipe == null ) {
         addNotRecipe(inputs, taskElement, inputsNumbers, outputs, subtasks);
         
      }
      
      for (TempClass inp : inputs){         
         for(int h=0;h<inp.inputs.size();h++){
            String inputRef = inp.inputs.get(h);
            if(inputRef.contains(ReferenceFrame)){
               for(int s=0;s<inp.inputs.size();s++){
                  String inputDep = inp.inputs.get(s);
                  String modified = inp.steps.get(s).getType().getModified(inputDep.replaceAll("[0-9]$", ""));
                  if(!inputDep.contains(ReferenceFrame) && !inp.steps.get(s).equals(inp.steps.get(h)) && modified!=null){
                     //System.out.println(inp.steps.get(s).getType().getId()+" "+inp.steps.get(h).getType().getId());
                     Element orderStep = findNode(subtasks, "step","name",inp.stepNames.get(h));
                     
                        if(orderStep.getAttribute("requires")==null || orderStep.getAttribute("requires")==""){
                           Attr requires = document.createAttribute("requires");
                           requires.setValue(inp.stepNames.get(s));
                           orderStep.setAttributeNode(requires);
                        }
                        else{
                           orderStep.setAttribute("requires",orderStep.getAttribute("name")+" "+inp.stepNames.get(s) );
                        }
                     
                     
                  }
               }
            }
         }
      }
      
      
      for (Entry<String, String> binding : bindingsInputs.entrySet()) {

         for (TempClass inputEntry : inputs) {
            for (int m = 0; m < inputEntry.inputs.size(); m++) {
               String inputListName = inputEntry.inputs.get(m);

               // System.out.println(binding.getValue() + ": "+
               // inputEntry.getKey());
               // System.out.println(binding.getKey() + ": "
               // +inputListName[0].replaceAll("[0-9]*$", ""));

               if ( inputEntry.name.equals(binding.getValue())
                  && binding.getKey().endsWith(
                        inputListName.replaceAll("[0-9]$", "")) ) {
                  Element subtaskBinding = document.createElementNS(xmlnsValue,
                        "binding");
                  // subtasks.appendChild(subtaskBinding);
                  Attr bindingSlot = document.createAttribute("slot");
                  bindingSlot.setValue(binding.getKey());
                  subtaskBinding.setAttributeNode(bindingSlot);

                  Attr bindingValue = document.createAttribute("value");

                  bindingValue.setValue("$this." + inputListName);
                  subtaskBinding.setAttributeNode(bindingValue);

                  subtasks.appendChild(subtaskBinding);
               }
            }
         }

      }
      
     
      
      for (Entry<String, String> bind : bindingsOutputs.entrySet()) {
         Element subtaskBinding = document.createElementNS(xmlnsValue,
               "binding");
         // subtasks.appendChild(subtaskBinding);
         Attr bindingSlot = document.createAttribute("slot");
         bindingSlot.setValue(bind.getKey());
         subtaskBinding.setAttributeNode(bindingSlot);

         Attr bindingValue = document.createAttribute("value");

         bindingValue.setValue(bind.getValue());
         subtaskBinding.setAttributeNode(bindingValue);

         subtasks.appendChild(subtaskBinding);
      }
      
      
   }

   private void addNotRecipe (ArrayList<TempClass> inputs, Element taskElement,
         List<String> inputsNumbers, Map<String, Task> outputs,
         Element subtasks) {
      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            Element inputTask = document.createElementNS(xmlnsValue, "input");
            taskElement.insertBefore(inputTask, taskElement.getFirstChild());
            Attr inputName = document.createAttribute("name");
            boolean contain = false;
            for (String str : inputsNumbers) {
               if ( str.contains(inputListName + "1") ) {
                  contain = true;
                  break;
               }
            }
            if ( !contain ) {

               inputName.setValue(inputListName + "1");
               inputsNumbers.add(inputListName + "1");
               inputEntry.inputs.set(m, inputListName + "1");

            } else {
               for (int i = inputsNumbers.size() - 1; i >= 0; i--) {
                  if ( inputsNumbers.get(i).contains(inputListName) ) {
                     int start = inputsNumbers.get(i).lastIndexOf(
                           inputsNumbers.get(i))
                        + inputsNumbers.get(i).length() - 1;
                     int end = inputsNumbers.get(i).length();
                     String number = inputsNumbers.get(i).substring(start, end);
                     int num = Integer.parseInt(number);
                     num++;
                     inputsNumbers.add(inputListName + num);
                     inputName.setValue(inputListName + num);
                     inputEntry.inputs.set(m, inputListName + num);
                     break;
                  }
               }
            }
            inputTask.setAttributeNode(inputName);
            Attr inputType = document.createAttribute("type");

            inputType.setValue(inputEntry.steps.get(m).getType()
                  .getSlotType(inputListName));
            inputTask.setAttributeNode(inputType);
            
            String modified = inputEntry.steps.get(m).getType().getModified(inputListName);
            if(modified!=null && modified!=""){
            for(Entry<String, Task> out:outputs.entrySet()){
               if(out.getValue().equals(inputEntry.steps.get(m))){
                  
                     Attr modifiedAttr = document.createAttribute("modified");
                     
                     modifiedAttr.setValue(out.getKey());
                     inputTask.setAttributeNode(modifiedAttr);
                     
                  }
               }
            }
         }

      }
      
      Element endInput = null;
      for (int i = 0; i < taskElement.getChildNodes().getLength(); i++) {
         if ( taskElement.getChildNodes().item(i).getNodeName()
               .equals("subtasks") )
            endInput = (Element) taskElement.getChildNodes().item(i);
      }
      for (Entry<String, Task> out : outputs.entrySet()) {
         Element outputTask = document.createElementNS(xmlnsValue, "output");
         taskElement.insertBefore(outputTask, endInput);
         Attr outputName = document.createAttribute("name");
         outputName.setValue(out.getKey());
         outputTask.setAttributeNode(outputName);
         Attr outputType = document.createAttribute("type");
         outputType.setValue(out.getValue().getType().getSlotType(out.getKey().replaceAll("[0-9]$", "")));
         outputTask.setAttributeNode(outputType);
      }

      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);

            Element subtaskBinding = document.createElementNS(xmlnsValue,
                  "binding");
            // subtasks.appendChild(subtaskBinding);
            Attr bindingSlot = document.createAttribute("slot");
            bindingSlot.setValue("$this." + inputListName);
            subtaskBinding.setAttributeNode(bindingSlot);

            Attr bindingValue = document.createAttribute("value");

            bindingValue.setValue(inputEntry.name);
            subtaskBinding.setAttributeNode(bindingValue);

            subtasks.appendChild(subtaskBinding);

         }
      }

   }

   private void addRecipe (Element taskElement, String input, Element subtasks,
         ArrayList<TempClass> inputs, Element recipe) {
      taskElement = recipe;
      Element applicable = document.createElementNS(xmlnsValue, "applicable");
      applicable.setTextContent("!$this." + input);
      subtasks.appendChild(applicable);

      for (TempClass inputEntry : inputs) {
         for (int m = 0; m < inputEntry.inputs.size(); m++) {
            String inputListName = inputEntry.inputs.get(m);
            for (String ins : recipeTaskClass.getDeclaredInputNames()) {
               boolean change = false;
               if ( recipeTaskClass.getSlotType(ins).equals(
                     inputEntry.steps.get(m).getType()
                           .getSlotType(inputEntry.inputs.get(m)))
                  && ins.contains(inputListName) ) {
                  String findParent = findValueOfInput(ins,
                        recipeTaskClass.getId());
                  if ( findParent != null && findParent.equals(inputEntry.name) ) {
                     inputEntry.inputs.set(m, ins);
                     change = true;
                     // System.out.println("---" + inputEntry.getKey() + " "
                     // + ins);
                  }

                  // if we cannot find the value in it's parents, it may be
                  // in it's siblings
                  if ( !change ) {

                     List<DecompositionClass> decompositions = recipeTaskClass
                           .getDecompositions();
                     for (DecompositionClass subtaskDecomposition : decompositions) {

                        boolean breaking = false;

                        Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                              .getBindings().entrySet();
                        for (Entry<String, Binding> binding : bindingsSubtask) {

                           if ( binding.getKey().equals("$this." + ins) ) {

                              if ( binding.getValue().value
                                    .equals(inputEntry.name) ) {
                                 inputEntry.inputs.set(m, ins);
                                 // System.out
                                 // .println("---" + inputListName[0]);

                              }

                           }

                        }
                     }
                  }
               }
            }
         }
      }

   }

   private String findValueOfInput (String in, String parent) {

      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      while (tasksIterator.hasNext()) {
         TaskClass taskclass = tasksIterator.next();
         List<DecompositionClass> decompositions = taskclass
               .getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {

            for (String stepName : subtaskDecomposition.getStepNames()) {

               if ( subtaskDecomposition.getStepType(stepName).getId()
                     .equals(parent) ) { // I should add namespace
                  Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                        .getBindings().entrySet();
                  for (Entry<String, Binding> binding : bindingsSubtask) {

                     if ( binding.getKey().contains(stepName)
                        && binding.getKey().contains(in) ) {
                        Collection<Entry<String, Binding>> bindingsSubtask2 = subtaskDecomposition
                              .getBindings().entrySet();
                        String val = binding.getValue().value;
                        for (Entry<String, Binding> binding2 : bindingsSubtask2) {

                           if ( binding2.getKey().contains(val) ) {
                              if ( !binding2.getValue().value.contains("$this") ) {
                                 return binding2.getValue().value;
                              } else {
                                 return findValueOfInput(val.substring(6),
                                       subtaskDecomposition.getGoal().getId());
                              }
                           }

                        }
                        return findValueOfInput(val.substring(6),
                              subtaskDecomposition.getGoal().getId());
                     }

                  }
               }
            }

         }
      }
      return null;
   }

   public Element learnedTaskmodelToDom (List<String> namespaces,
         String taskName, List<Task> steps, Element taskModelElement,
         String input) {

      Element recipe = null;
      boolean recipeOccured = true;
      Iterator<TaskClass> tasksIterator = this.taskModel.getTaskClasses()
            .iterator();
      ArrayList<Task> changedTasks = new ArrayList<Task>();

      while (tasksIterator.hasNext()) {

         TaskClass task = tasksIterator.next();

         Element taskElement = document.createElementNS(xmlnsValue, "task");
         taskModelElement.appendChild(taskElement);
         Attr idTask = document.createAttribute("id");
         idTask.setValue(task.getId());
         taskElement.setAttributeNode(idTask);
         if ( task.getId().equals(taskName) ) {
            recipe = taskElement;
            recipeTaskClass = task;
         }

         boolean changedTask = false;
         for (Task step : steps) {
            String stepType = step.getType().getId();
            if ( task.getId().equals(stepType) ) {
               changedTasks.add(step);
               changedTask = true;
               break;
            }
         }

         for (String inputName : task.getDeclaredInputNames()) {
            Element inputTask = document.createElementNS(xmlnsValue, "input");
            taskElement.appendChild(inputTask);
            Attr inputNameAttr = document.createAttribute("name");
            inputNameAttr.setValue(inputName);
            inputTask.setAttributeNode(inputNameAttr);

            Attr inputType = document.createAttribute("type");
            inputType.setValue(task.getSlotType(inputName));
            inputTask.setAttributeNode(inputType);

            String modified = task.getModified(inputName);
            //System.out.println("modified: "+modified);
            if ( modified != null && modified!="" ) {
               Attr modifiedAttr = document.createAttribute("modified");
               modifiedAttr.setValue(modified);
               inputTask.setAttributeNode(modifiedAttr);
            }
         }

         for (String outputName : task.getDeclaredOutputNames()) {
            Element inputTask = document.createElementNS(xmlnsValue, "output");
            taskElement.appendChild(inputTask);
            Attr inputNameAttr = document.createAttribute("name");
            inputNameAttr.setValue(outputName);
            inputTask.setAttributeNode(inputNameAttr);
            Attr inputType = document.createAttribute("type");
            inputType.setValue(task.getSlotType(outputName));
            inputTask.setAttributeNode(inputType);
         }

         List<DecompositionClass> decompositions = task.getDecompositions();
         for (DecompositionClass subtaskDecomposition : decompositions) {

            Element subtasks = document.createElementNS(xmlnsValue, "subtasks");
            taskElement.appendChild(subtasks);
            Attr idSubtask = document.createAttribute("id");
            String name = subtaskDecomposition.getId();

            for (String stepName : subtaskDecomposition.getStepNames()) {

               Element subtaskStep = document.createElementNS(xmlnsValue,
                     "step");
               subtasks.appendChild(subtaskStep);
               Attr nameSubtaskStep = document.createAttribute("name");

               String stepStrValue = stepName;
               nameSubtaskStep.setValue(stepStrValue);
               subtaskStep.setAttributeNode(nameSubtaskStep);
               Attr valueSubtaskStep = document.createAttribute("task");
               String namespaceDec = subtaskDecomposition.getStepType(stepName)
                     .getNamespace();
               String[] dNSNameArrayDec = namespaceDec.split(":");
               String dNSNameDec = dNSNameArrayDec[dNSNameArrayDec.length - 1];

               if ( dNSNameDec.compareTo(namespacePrefix) != 0 )
                  valueSubtaskStep.setValue(dNSNameDec + ":"
                     + subtaskDecomposition.getStepType(stepName).getId());
               else
                  valueSubtaskStep.setValue(subtaskDecomposition.getStepType(
                        stepName).getId());
               subtaskStep.setAttributeNode(valueSubtaskStep);

               if ( !namespaces.contains(subtaskDecomposition.getStepType(
                     stepName).getNamespace())
                  && !subtaskDecomposition.getStepType(stepName).getNamespace()
                        .equals(namespace) ) {
                  namespaces.add(subtaskDecomposition.getStepType(stepName)
                        .getNamespace());
               }
            }

            Collection<Entry<String, Binding>> bindingsSubtask = subtaskDecomposition
                  .getBindings().entrySet();

            if ( recipe != null && recipeOccured ) {
               Element applicable = document.createElementNS(xmlnsValue,
                     "applicable");
               applicable.setTextContent("$this." + input);
               subtasks.appendChild(applicable);
               recipeOccured = false;
            }

            if ( subtaskDecomposition.applicable != null
               && subtaskDecomposition.applicable != "" ) {
               Element applicable = document.createElementNS(xmlnsValue,
                     "applicable");
               applicable.setTextContent(subtaskDecomposition.applicable);
               subtasks.appendChild(applicable);
            }

            for (Entry<String, Binding> binding : bindingsSubtask) {
               Element subtaskBinding = document.createElementNS(xmlnsValue,
                     "binding");
               // subtasks.appendChild(subtaskBinding);
               Attr bindingSlot = document.createAttribute("slot");

               bindingSlot.setValue(binding.getKey());

               subtaskBinding.setAttributeNode(bindingSlot);

               Attr bindingValue = document.createAttribute("value");

               bindingValue.setValue(binding.getValue().value);
               // else
               // bindingValue.setValue("$this."+);
               subtaskBinding.setAttributeNode(bindingValue);
               if ( changedTask == false || !binding.getKey().contains("this") )
                  subtasks.appendChild(subtaskBinding);
            }

            subtasks.setAttributeNode(idSubtask);
            idSubtask.setValue(name);

         }

      }
      return recipe;
   }
   
   private Element findNode(Element parent, String nodeName, String name, String value){
      for(int i=0;i<parent.getChildNodes().getLength();i++){
         Element child = (Element) parent.getChildNodes().item(i);
      
         if(child.getNodeName().equals(nodeName)){
            String attrVal = child.getAttribute(name);
            if(value.equals(attrVal)){
               return child;
            }
         }
      }
      return null;
   }
   
 
}

