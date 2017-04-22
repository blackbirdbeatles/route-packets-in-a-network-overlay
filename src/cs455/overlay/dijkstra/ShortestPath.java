package cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by MyGarden on 17/2/9.
 */

//Uisng dijkstra algorithm

public class ShortestPath {

    HashMap<String, Integer> estimate;
    HashMap<String, Integer> already;
    HashMap<String, String> parentList;
    HashMap<String, String> nextHop;
    HashMap<String, HashMap<String, Integer>> adjList;

    ArrayList<String> allHostArray;

    String source;
    //Your nextHop is your parent's nextHop, unless your parent is "source" (myID);

    public String findMin(HashMap<String, Integer> est){
        Integer min = Integer.MAX_VALUE;
        String minHost = "NoHost";
        for (String host: est.keySet()){
            Integer distance = est.get(host);
            if (distance > 0 && distance < min){
                min = distance;
                minHost = host;
            }
        }
        return minHost;
    }

    public void relax(String selectedHost){
        HashMap<String, Integer> neighbors = adjList.get(selectedHost);
        for (String neighbor: neighbors.keySet()){
            Integer tentativeDistanceViaSelectedHost = estimate.get(selectedHost) + adjList.get(selectedHost).get(neighbor);
            if (estimate.containsKey(neighbor)) {
                if (estimate.get(neighbor) == -1) {
                    estimate.put(neighbor, tentativeDistanceViaSelectedHost);
                    parentList.put(neighbor, selectedHost);
                } else if (tentativeDistanceViaSelectedHost < estimate.get(neighbor)) {
                    estimate.put(neighbor, tentativeDistanceViaSelectedHost);
                    parentList.put(neighbor, selectedHost);
                }
            }
        }
    }

    public void printPathOf(String host){
        if (!host.equals(source)){
            printPathOf(parentList.get(host));
            System.out.print("--" + adjList.get(parentList.get(host)).get(host) + "--" + host);
        }
    }

    public ShortestPath(ArrayList<ArrayList<Object>> linkWeightList, String source){

        estimate = new HashMap<>();
        already = new HashMap<>();
        adjList = new HashMap<>();
        parentList = new HashMap<>();
        nextHop = new HashMap<>();
        this.source = source;
        //Get all host list
        HashSet<String> allHost = new HashSet<String>();
        for (ArrayList<Object> linkInfo: linkWeightList){
            String from = (String) linkInfo.get(0);
            String to = (String) linkInfo.get(1);
            Integer weight = (Integer) linkInfo.get(2);
            allHost.add(from);
            allHost.add(to);
        }

        //initiate adjList according to all-host list
        allHostArray = new ArrayList<>(allHost);

        for (String host:allHostArray) {
            adjList.put(host, new HashMap<String, Integer>());
            estimate.put(host, -1);
            parentList.put(host,"NoParent");
            nextHop.put(host,"NoNextHop");
        }

        //COPY AND ADD THE REVERSE EDGE FROM THE LINKWEIGHT LIST
        //Meanwhile, initiate estimate, parentList
        for (ArrayList<Object> linkInfo: linkWeightList){
            String from = (String) linkInfo.get(0);
            String to = (String) linkInfo.get(1);
            Integer weight = (Integer) linkInfo.get(2);

            //adjacent list
            adjList.get(from).put(to, weight);
            adjList.get(to).put(from, weight);


            if (from.equals(source)) {
                //estimate
                estimate.put(to, weight);

                //parentList
                parentList.put(to, from);
            }
        }

        //Dijkstra core algorithm
        while (!estimate.isEmpty()){
            String selected = findMin(estimate);
            already.put(selected, estimate.get(selected));
            if (parentList.get(selected).equals(source))
                nextHop.put(selected,selected);
            else
                nextHop.put(selected, nextHop.get(parentList.get(selected)));
            relax(selected);
            estimate.remove(selected);
        }

    }

    public HashMap<String, String> getRountingPlan(){
        return this.nextHop;
    }

    public void printPath(){
        for (String host: allHostArray){
            if (!host.equals(source)){
                System.out.print(source);
                printPathOf(host);
                System.out.println("");
            }
        }
    }

    public ArrayList<String> getAllHostArray(){
        return  allHostArray;
    }
    public void printRoutingPlan(){
        for (String host: nextHop.keySet())
            System.out.println(host + " " + nextHop.get(host));

    }

}
