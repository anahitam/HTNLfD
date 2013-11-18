package edu.wpi.htnlfd.graph;

import java.util.*;
import edu.wpi.htnlfd.Demonstration;
import edu.wpi.htnlfd.model.DecompositionClass.Step;
import edu.wpi.htnlfd.model.*;

public class Graph {

   Node startNode = null;

   Node endNode = null;

   List<Component> components;

   static Map<String, Graph> taskGraphs = new HashMap<String, Graph>();

   public Graph (String taskName) {
      if ( !taskGraphs.containsKey(taskName) ) {

         this.startNode = new Node();
         this.endNode = new Node();
         components = new ArrayList<Component>();
         taskGraphs.put(taskName, this);
      } else {
         this.startNode = taskGraphs.get(taskName).startNode;
         this.endNode = taskGraphs.get(taskName).endNode;
         this.components = taskGraphs.get(taskName).components;
      }
   }

   public static Graph getGraph (String taskName) {
      if ( !taskGraphs.containsKey(taskName) ) {
         return new Graph(taskName);
      } else {
         return taskGraphs.get(taskName);
      }
   }

   void getLCS (List<Node> nodes1, List<Node> nodes2, TaskModel taskModel,
         List<Node> newNodes1, List<Node> newNodes2) {
      int M = nodes1.size();
      int N = nodes2.size();
      int[][] opt = new int[M + 1][N + 1];

      int LCS = findLCS(nodes1, nodes2, taskModel, opt);

      int i = 0, j = 0;

      while (i < M && j < N) {
         if ( nodes1.get(i).isEquivalent(nodes2.get(j), taskModel) ) {

            newNodes1.add(nodes1.get(i));
            newNodes2.add(nodes2.get(j));

            i++;
            j++;
         } else {

            if ( opt[i + 1][j] >= opt[i][j + 1] ) {
               i++;
            } else {

               j++;
            }
         }
      }

   }

   /**
    * Main function to merge two taskclasses.
    * 
    * @return
    */
   /*
    * public boolean addGraph (Demonstration demonstration, TaskClass task,
    * TaskModel taskModel, TaskClass newTask) { List<ArrayList<Node>> nodesLists
    * = new ArrayList<ArrayList<Node>>(); List<Node> newNodes = new
    * ArrayList<Node>(); List<Node> nodesP = new ArrayList<Node>();
    * nodesP.add(startNode); getNodes(task, taskModel, nodesP); //bfs(nodesP);
    * findPathes(nodesLists, startNode); newNodes.add(startNode); // it should
    * be here getNodes(newTask, taskModel, newNodes); int max = 0; int[][]
    * maxSolution = null; List<Node> chosenNodes = null; List<Node>
    * chosenDemNodes = null; List<ArrayList<Node>> nodesListDem = new
    * ArrayList<ArrayList<Node>>(); giveCombinations(nodesListDem,
    * newNodes.subList(1, newNodes.size())); //
    * nodesListDem.add((ArrayList<Node>) newNodes); for (ArrayList<Node> dem :
    * nodesListDem) { dem.add(0, startNode); for (int i = 0; i <
    * nodesLists.size(); i++) { List<Node> nodes = nodesLists.get(i); int M =
    * nodes.size() - 1; int N = dem.size() - 1; int[][] opt = new int[M + 1][N +
    * 1]; int LCS = findLCS(nodes.subList(1, nodes.size()), dem.subList(1,
    * dem.size()), taskModel, opt); if ( max < LCS ) { chosenNodes = nodes;
    * chosenDemNodes = dem; maxSolution = opt; max = LCS; } //
    * System.out.println("LCS = " + LCS); } } if(max == 0){ return false; }
    * merge(chosenNodes.subList(1, chosenNodes.size()),
    * chosenDemNodes.subList(1, chosenDemNodes.size()), taskModel, maxSolution);
    * bfs(chosenNodes); boolean opt = optional(taskModel); boolean alt =
    * alternativeRecipe(demonstration, taskModel, task, newTask); return alt ||
    * opt; }
    */

   public boolean addGraph (Demonstration demonstration, TaskClass task,
         TaskModel taskModel, TaskClass newTask) {

      List<Node> nodesP = new ArrayList<Node>();

      // nodesP.add(startNode);
      getNodes(newTask, taskModel, nodesP);

      if ( this.components.size() == 0 ) {
         Component component = new Component(1, this.startNode, this.endNode);
         this.components.add(component);
         component.join(nodesP, this, taskModel);

         // component.bfs();
      } else {
         boolean canJoin = false;

         for (int i = 0; i < this.components.size(); i++) {
            if ( this.components.get(i).canJoin(nodesP, this, taskModel) ) {

               this.components.get(i).join(nodesP, this, taskModel);
               canJoin = true;
               break;
            }
         }

         if ( !canJoin ) {
            int max = 0;
            for (Component comp : this.components) {
               max = comp.getId() > max ? comp.getId() : max;
            }
            Component component = new Component(max + 1, this.startNode,
                  this.endNode);
            this.components.add(component);
            component.join(nodesP, this, taskModel);

            // component.bfs();
         }
      }

      boolean opt = optional(taskModel, startNode);
      /*
       * boolean alt = alternativeRecipe(demonstration, taskModel, task,
       * newTask); return alt || opt;
       */
      return false;
   }

   /**
    * Finds pathes.
    */
   void findPathes (List<ArrayList<Node>> nodesLists, Node start) {
      List<Node> newNodes = new ArrayList<Node>();
      nodesLists.add((ArrayList<Node>) newNodes);
      newNodes.add(start); // startNode
      branch(nodesLists, newNodes, start);

   }

   /**
    * Finds pathes with a recursive function.
    */
   void branch (List<ArrayList<Node>> nodesLists, List<Node> nodes, Node root) {

      if ( root.childs.size() == 1 ) {
         nodes.add(root.childs.get(0));
         branch(nodesLists, nodes, root.childs.get(0));
      } else if ( root.childs.size() > 1 ) {
         int size = nodes.size();
         nodes.add(root.childs.get(0));
         branch(nodesLists, nodes, root.childs.get(0));

         for (int i = 1; i < root.childs.size(); i++) {

            List<Node> newNodes = new ArrayList<Node>();
            for (int k = 0; k < size; k++) {
               /*
                * Node node = nodes.get(k); Node temp = null; if ( node.step !=
                * null ) { temp = new Node(node.step, node.tasks.get(0),
                * node.decompositions.get(0), node.stepNames.get(0)); } else {
                * temp = new Node(node.step, null, null, null); }
                * temp.parents.addAll(node.parents);
                * temp.childs.addAll(node.childs);
                */
               newNodes.add(nodes.get(k));

            }
            nodesLists.add((ArrayList<Node>) newNodes);
            newNodes.add(root.childs.get(i));
            branch(nodesLists, newNodes, root.childs.get(i));

         }
      }

   }

   /**
    * Converts TaskClass to Node.
    */
   public void getNodes (TaskClass task, TaskModel taskModel, List<Node> nodes) {

      // adding optional steps and alternative recipes

      for (DecompositionClass dec : task.getDecompositions()) {

         for (String stepName : dec.getStepNames()) {
            Node newNode1 = new Node(dec.getStep(stepName), task, dec, stepName);
            nodes.add(newNode1);
         }
      }

      for (int i = 0; i < nodes.size(); i++) {

         if ( i - 1 >= 0 ) {
            if ( (nodes.get(i - 1).decompositions.size() != 0
               && nodes.get(i).decompositions.size() != 0 && nodes.get(i).decompositions
                  .get(0).equals(nodes.get(i - 1).decompositions.get(0)))
               || (nodes.get(i - 1).decompositions.size() == 0 && nodes.get(i).decompositions
                     .size() == 0) || nodes.get(i - 1).equals(startNode) )
               nodes.get(i).parents.add(nodes.get(i - 1));
            if ( nodes.get(i - 1).stepNames.size() != 0
               && nodes.get(i).stepNames.size() != 0
               && !nodes.get(i).decompositions.get(0).isOptionalStep(
                     nodes.get(i).stepNames.get(0))
               && nodes.get(i - 1).decompositions.get(0).isOptionalStep(
                     nodes.get(i - 1).stepNames.get(0)) ) {
               int j = i - 1;
               while (nodes.get(j).decompositions.get(0).isOptionalStep(
                     nodes.get(j).stepNames.get(0))) {
                  j = j - 1;
               }

               nodes.get(i).parents.add(nodes.get(j));
               nodes.get(j).childs.add(nodes.get(i));
            }
         }

         if ( i + 1 < nodes.size() )
            if ( (nodes.get(i).decompositions.size() != 0
               && nodes.get(i + 1).decompositions.size() != 0 && nodes.get(i).decompositions
                  .get(0).equals(nodes.get(i + 1).decompositions.get(0)))
               || (nodes.get(i).decompositions.size() == 0 && nodes.get(i + 1).decompositions
                     .size() == 0) || nodes.get(i).equals(startNode) )
               nodes.get(i).childs.add(nodes.get(i + 1));

      }

      /*
       * boolean contain = true; while(contain){ contain = false; for(int
       * i=0;i<nodes.size();i++){ Node nod = nodes.get(i); if(nod.step!=null &&
       * (nod.step.getType().isInternal() ||
       * nod.step.getType().getId().startsWith("_"))){ List<Node> chNodes = new
       * ArrayList<Node>(); getNodes ( nod.step.getType(), taskModel, chNodes);
       * for(Node chn:chNodes){ if(chn.parents == null ||
       * chn.parents.size()==0){ chn.parents.addAll(nod.parents); for(Node
       * par:nod.parents){ for(Node pc:par.childs) if(pc.step.equals(nod.step))
       * par.childs.remove(pc); // check step par.childs.add(chn); } } }
       * if(chNodes.size()>=1) for(Node chn:chNodes){ if(chn.childs == null ||
       * chn.childs.size()==0){ chn.childs.addAll(nod.childs); for(Node
       * ch:nod.childs){ for(Node cp:ch.parents) if(cp.step.equals(nod.step))
       * ch.parents.remove(cp); // check step ch.parents.add(chn); } } }
       * nodes.remove(i); nodes.addAll(i,chNodes); contain = true; break; } } }
       */
   }

   void mergeGraphs (List<Node> x, List<Node> y, TaskModel taskModel,
         Component component) {

      for (int k = 0; k < x.size(); k++) {
         y.get(k).tasks.get(0).setId("PlaceDishes111");
         x.get(k).tasks.add(y.get(k).tasks.get(0));
         x.get(k).decompositions.add(y.get(k).decompositions.get(0));
         x.get(k).stepNames.add(y.get(k).stepNames.get(0));

         // handling parents
         for (Node ypar : y.get(k).parents) {
            boolean contain = false;
            for (Node xpar : x.get(k).parents) {
               if ( xpar.equals(ypar) || xpar.step.equals(ypar.step) ) {
                  // ypar.printNode();
                  // xpar.printNode();
                  contain = true;
                  break;
               }
            }
            if ( !contain ) {
               boolean contC = false;
               for (int l = 0; l <= k - 1; l++) {
                  if ( y.get(l).equals(ypar) || y.get(l).step.equals(ypar.step) ) {
                     boolean contain1 = false;
                     for (Node xpar : x.get(k).parents) {
                        if ( xpar.equals(x.get(l))
                           || xpar.step.equals(x.get(l).step) ) {
                           contain1 = true;
                           break;
                        }
                     }
                     if ( !contain1 ) {
                        x.get(k).parents.add(x.get(l));

                     }

                     boolean contain2 = false;
                     for (Node xpar : x.get(l).childs) {
                        if ( xpar.equals(x.get(k))
                           || xpar.step.equals(x.get(k).step) ) {
                           contain2 = true;
                           break;
                        }
                     }
                     if ( !contain2 ) {
                        x.get(l).childs.add(x.get(k));
                     }

                     contC = true;
                     break;
                  }
               }
               if ( !contC ) {
                  x.get(k).parents.add(ypar);

               }
               ypar.childs.add(x.get(k));

            }
            for (Node ch : ypar.childs) {
               if ( ch.equals(y.get(k)) || ch.step.equals(y.get(k).step) ) {
                  ypar.childs.remove(ch);

                  break;
               }
            }
         }

         // handling children
         for (Node ych : y.get(k).childs) {
            boolean contain = false;
            for (Node xch : x.get(k).childs) {
               // ych.printNode();
               // xch.printNode();
               if ( xch.equals(ych) || xch.step.equals(ych.step) ) {
                  contain = true;
                  break;
               }
            }
            if ( !contain ) { //
               boolean contC = false;
               for (int l = k + 1; l < y.size(); l++) {
                  if ( y.get(l).equals(ych) || y.get(l).step.equals(ych.step) ) {
                     boolean contain1 = false;
                     for (Node xch : x.get(k).childs) {
                        if ( xch.equals(x.get(l))
                           || xch.step.equals(x.get(l).step) ) {
                           contain1 = true;
                           break;
                        }
                     }
                     if ( !contain1 ) {
                        x.get(k).childs.add(x.get(l));

                     }
                     boolean contain2 = false;
                     for (Node xch : x.get(l).parents) {
                        if ( xch.equals(x.get(k))
                           || xch.step.equals(x.get(k).step) ) {
                           contain2 = true;
                           break;
                        }
                     }
                     if ( !contain2 ) {
                        x.get(l).parents.add(x.get(k));
                     }
                     contC = true;
                     break;
                  }
               }
               if ( !contC ) {

                  boolean cont = false;
                  for (Node par : ych.parents) {
                     if ( par.equals(x.get(k))
                        || par.step.equals(x.get(k).step) ) {
                        cont = true;
                        break;
                     }
                  }
                  if ( !cont ) { //
                     ych.parents.add(x.get(k));
                  }
                  x.get(k).childs.add(ych);
                  for (int l = 0; l < ych.parents.size(); l++) {
                     if ( ych.parents.get(l).step.equals(y.get(k).step) ) {
                        ych.parents.remove(ych.parents.get(l));
                        break;
                     }
                  }
               }

            }

         }

      }
   }

   /**
    * Finds longest comment sequence.
    */
   public static int findLCS (List<Node> x, List<Node> y, TaskModel taskModel,
         int[][] opt) {

      int M = x.size();
      int N = y.size();
      int LCS = 0;
      for (int i = M - 1; i >= 0; i--) {
         for (int j = N - 1; j >= 0; j--) {
            if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
               opt[i][j] = opt[i + 1][j + 1] + 1;

            } else
               opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
         }
      }

      int i = 0, j = 0;
      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
            i++;
            j++;
            LCS++;
         } else {

            if ( opt[i + 1][j] >= opt[i][j + 1] ) {
               i++;
            } else {
               j++;
            }
         }
      }
      return LCS;
   }

   /**
    * Find alternative recipe.
    */
   public int[][] findAlternativeRecipe (List<Node> x, List<Node> y,
         TaskModel taskModel) {

      int M = x.size();
      int N = y.size();
      int[][] interval = new int[2][2];

      int i = 0, j = 0;
      int startF = -1;
      int endF = x.size(); // ??
      int startS = -1;
      int endS = y.size(); // ??
      while (i < M && j < N) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
            startF = i;
            startS = j;
            i++;
            j++;
         } else
            break;
      }
      i = M - 1;
      j = N - 1;
      while (i >= 0 && j >= 0) {
         if ( x.get(i).isEquivalent(y.get(j), taskModel) ) {
            endF = i;
            endS = j;
            i--;
            j--;
         } else
            break;
      }
      i = startF + 1;
      j = startS + 1;

      int M1 = x.size();
      int N1 = y.size();
      int[][] opt = new int[M1 + 1][N1 + 1];

      int LCS = findLCS(x, y, taskModel, opt);
      if ( LCS != startF + x.size() - endF + 1 ) {
         interval[0][0] = -1;
         interval[0][1] = -1;
         interval[1][0] = -1;
         interval[1][1] = -1;
         return interval;
      }

      interval[0][0] = startF;
      interval[0][1] = endF;
      interval[1][0] = startS;
      interval[1][1] = endS;

      return interval;
   }

   /**
    * Depth first search
    */

   /**
    * Gives combinations of the nodes by considering their ordering constraints.
    */
   void giveCombinations (List<ArrayList<Node>> nodesLists, List<Node> dem) {
      combination(nodesLists, dem, new ArrayList<Node>());

      for (List<Node> nodeLists : nodesLists) {
         for (int m = 1; m < nodeLists.size(); m++) {
            nodeLists.get(m).parents.clear();
            nodeLists.get(m).parents.add(nodeLists.get(m - 1));
            nodeLists.get(m - 1).childs.clear();
            nodeLists.get(m - 1).childs.add(nodeLists.get(m));
         }
      }
   }

   /**
    * Recursive function for finding combinations.
    */
   void combination (List<ArrayList<Node>> nodesLists, List<Node> dem,
         List<Node> nodes) {

      if ( nodes.size() == dem.size() ) {
         nodesLists.add((ArrayList<Node>) nodes);

         return;
      }

      // checking ordering constraints
      if ( nodes.size() != 0 && nodes.get(nodes.size() - 1).step != null
         && nodes.get(nodes.size() - 1).step.getRequired() != null
         && nodes.get(nodes.size() - 1).step.getRequired().size() != 0 ) {
         for (String reqStep : nodes.get(nodes.size() - 1).step.getRequired()) {
            boolean contain = false;
            for (Node req : nodes) {
               if ( req.stepNames.get(0).equals(reqStep) ) {
                  contain = true;
               }
            }
            if ( !contain ) {
               return;
            }
         }
      }

      for (int i = 0; i < dem.size(); i++) {
         boolean contain = false;
         for (int j = 0; j < nodes.size(); j++) {
            if ( dem.get(i).step.equals(nodes.get(j).step) ) {
               contain = true;
               break;
            }
         }
         if ( !contain ) {
            List<Node> newNodes = new ArrayList<Node>();
            for (Node node : nodes) {
               if ( node.step != null ) {
                  Node temp = new Node(node.step, node.tasks.get(0),
                        node.decompositions.get(0), node.stepNames.get(0));

                  newNodes.add(temp);
               } else {
                  Node temp = new Node(node.step, null, null, null);

                  newNodes.add(temp);
               }
            }
            Node temp = new Node(dem.get(i).step, dem.get(i).tasks.get(0),
                  dem.get(i).decompositions.get(0), dem.get(i).stepNames.get(0));
            newNodes.add(temp);
            combination(nodesLists, dem, newNodes);
         }
      }

   }

   /**
    * Searches for optional steps, and changes their optional attribute.
    * 
    * @return
    */
   boolean optional (TaskModel taskModel, Node root) {
      boolean opt = false;

      while (root.childs.size() != 0) {
         if ( root.childs.size() == 1 ) {
            root = root.childs.get(0);
         } else if ( root.childs.size() > 1 ) {
            boolean find = false;
            for (Node ch1 : root.childs) {
               for (Node ch2 : root.childs) {
                  if ( !ch1.equals(ch2) ) {
                     // not nested
                     Node node = ch2;
                     while (!node.childs.get(0).equals(ch1)) {

                        if ( node.childs.size() > 1 ) {

                           break;
                        }

                        node = node.childs.get(0);

                        if ( node == null || node.childs.size() == 0 )
                           break;
                     }

                     if ( node == null || node.childs.size() == 0 ) {
                        continue;
                     }
                     if ( node.childs.get(0).equals(ch1) ) {
                        node = ch2;
                        find = true;
                        while (!node.equals(ch1)) {
                           node.step.setMinOccurs(0);
                           node = node.childs.get(0);
                        }
                     }

                  }
               }
            }

            for (Node child : root.childs) {
               optional(taskModel, child);
            }

            break;
         }
      }
      return opt;

   }

   /**
    * Searches for alternative recipes and adds them.
    */
   boolean alternativeRecipe (Demonstration demonstration, TaskModel taskModel,
         TaskClass task, TaskClass newTask, Node root) {

      boolean alt = false;

      while (root.childs.size() != 0) {
         if ( root.childs.size() == 1 ) {
            root = root.childs.get(0);
         } else if ( root.childs.size() > 1 ) {

            for (Node child : root.childs) {
               alternativeRecipe(demonstration, taskModel, task, newTask, child);
            }

            break;
         }
      }
      return alt;

   }

   public void printGraph () {

      for (Component comp : this.components) {
         comp.bfs();
      }
   }
}
