/**
 * Class: IPLoM (Iterative Partitioning Log Mining)
 * 
 * Based on the log mining algorithms published on the following papers:
 * 
 * [1] Adetokunbo AO Makanju, A Nur Zincir-Heywood, and Evangelos E Milios. Clustering 
 * event logs using iterative partitioning. In Proceedings of the 15th ACM SIGKDD international 
 * conference on Knowledge discovery and data mining, pages 1255–1264. ACM, 2009.
 * 
 * [2] Adetokunbo Makanju, A Nur Zincir-Heywood, and Evangelos E Milios. A lightweight
 * algorithm for message type extraction in system application logs. Knowledge and Data
 * Engineering, IEEE Transactions on, 24(11):1921–1936, 2012.
 * 
 * @author ERICSSON/edghklj (Chang Liu)
 *
 * Initially Created: 2016-02-22
 *
 */

package iplom;

import static java.lang.System.out;
import java.io.*;
import java.util.*;



public class IPLoM {

  /* ------------------------------------------------------------------------------------ */
  /*                                    Elements                                          */
  /* ------------------------------------------------------------------------------------ */
  
  /**
   * Define the delimiter for separating a log message into tokens
   * Default: " []=:()/|\'\""
   */
  private String delimiter = " []=:()/|\'\"";
  
  /**
   * Define the partition support threshold
   * Default: 0.00
   */
  private double partitionSupportThreshold = 0;
  
  /**
   * Define the cluster goodness threshold
   * Default: 0.34
   */
  private double clusterGoodnessThreshold = 0.34;
  
  /**
   * Define the upper bound (>0.5) and lower bound (<0.5)
   * Default: upperBound = 0.9 | lowerBound = 0.1
   */
  private double upperBound = 0.9;
  private double lowerBound = 0.1;
  
  /**
   * Define the source file name (path)
   */
  private File sourceFile = null;
  
  
  /* ------------------------------------------------------------------------------------ */
  /*                                  Constructors                                        */
  /* ------------------------------------------------------------------------------------ */
  public IPLoM () { }
  
  public IPLoM (String fileName) {
    this.sourceFile = new File(fileName);
  }
  
  /* ------------------------------------------------------------------------------------ */
  /*                                    Methods TODO: optimize                            */
  /* ------------------------------------------------------------------------------------ */
  
  /**
   * Set the log file
   */
  public void setFile(String fileName) {
    this.sourceFile = new File(fileName);
  }
  
  /**
   * Print the log file name(path)
   */
  public File returnFile () {
    return this.sourceFile;
  }
  
  /**
   * Set the delimiter
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }
  
  /**
   * Set the partition support threshold
   */
  public void setPartitionSupportThreshold(double support) {
    this.partitionSupportThreshold = support;
  }
  
  /**
   * Set the cluster goodness
   */
  public void setClusterGoodnessThreshold(double goodness) {
    this.clusterGoodnessThreshold = goodness;
  }
  
  /**
   * Set upper bound
   */
  public void setUpperBound(double upperBound) {
    this.upperBound = upperBound;
  }
  
  /**
   * Set lower bound
   */
  public void setLowerBound(double lowerBound) {
    this.lowerBound = lowerBound;
  }
  
  /**
   * Set both the lower and upper bounds
   */
  public void setBounds(double lowerBound, double upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }
  
  /* ----------------------------------------------------------------------------------- */
  
  /**
   * Read the log file by lines
   * @param 
   * String fileName: input log file name(path)
   */
  public void readByLines() {
    BufferedReader reader = null;
    
    try {
      out.println("Read the file by lines.");
      reader = new BufferedReader(new FileReader(this.sourceFile));
      String tempString = null;
      int currentLine = 1;
      
      while ((tempString = reader.readLine()) != null) {
        singleLinePrint(tempString, currentLine);
        currentLine ++;
      }
      
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e1) {
        }
      }
    }
  }
  
  /**
   * Process a single line of the log
   * @param 
   * String str: input string
   * int currentLine: current line number
   */
  private void singleLinePrint(String str, int currentLine) {
    out.println("LINE " + currentLine + ": " + str);
    out.println("#Tokens: " + tokenSizeOfString(str));
  }
  
	
  /**
   * Count the #tokens of a string
   * @param 
   * String str: input string
   */
	private int tokenSizeOfString(String str) {
	  StringTokenizer tokens = new StringTokenizer(str, this.delimiter);
	  return tokens.countTokens();
	}

  /* ----------------------------------------------------------------------------------- */
  
	/**
	 * Partition the log messages based on the #tokens
	 * @param 
	 * 
	 */
  public Map<Integer, ArrayList<String>> partitionByTokenSize() {
    BufferedReader reader = null;
    Map<Integer, ArrayList<String>> partitionsBySize = new HashMap<>();
    
    try {
      out.println("Partition by token size.");
      reader = new BufferedReader(new FileReader(this.sourceFile));
      String tempString = null;
      //int currentLine = 1;
      int tokenSize = 0;
      while ((tempString = reader.readLine()) != null) {
        /* 
         * Remove the time stamp and server name here
         * TODO: other more intelligent way to get rid of time, server name, <number>, etc.
         */
        //tempString = tempString.substring(21, tempString.length());
        tokenSize = tokenSizeOfString(tempString);
        if (partitionsBySize.containsKey(tokenSize)) {
          partitionsBySize.get(tokenSize).add(tempString);
        } else {
          ArrayList<String> tempList = new ArrayList<>();
          tempList.add(tempString);
          partitionsBySize.put(tokenSize, tempList);
        }
        //currentLine ++;
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e1) {
        }
      }
    }
    
    return partitionsBySize;
  }
  
  
  /**
   * Print the sizePartitions
   * @param 
   */
  public void printSizePartition() {
    Map<Integer, ArrayList<String>> partitionsBySize = partitionByTokenSize();
    for (Map.Entry<Integer, ArrayList<String>> entry: partitionsBySize.entrySet()) {
      // out.println(entry.getKey() + " " + entry.getValue().size() + " " + entry.getValue());
      out.println(entry.getKey() + " " + entry.getValue().size());
      for (String oneLog: entry.getValue()) {
        out.println(oneLog);
      }
    }
  }
  
  
  
  /* ------------------------------------------------------------------------------------ */
  /*                          Partition by token position                                 */
  /* ------------------------------------------------------------------------------------ */
  /**
   * partitionByTokenPosition
   * @return 
   * Map<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionByPosition
   */
  public Map<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionByTokenPosition() {    
    
    out.println("Partition by token position.");
    
    Map<Integer, ArrayList<String>> partitionsBySize = partitionByTokenSize();
    Map<Integer, ArrayList<ArrayList<String>>> matirxBySize = new HashMap<>();
    Map<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionByPosition = new HashMap<>();
    
    /*
     * For each of the partition divided based on token size
     */
    for (Map.Entry<Integer, ArrayList<String>> partitionEntry: partitionsBySize.entrySet()) {
      
      out.println(partitionEntry.getKey() + " " + partitionEntry.getValue().size() + " " + partitionEntry.getValue());
      Integer tempSize = partitionEntry.getKey();
      matirxBySize.put(tempSize, new ArrayList<ArrayList<String>>());
      List<HashMap<String, Integer>> tokenCollection = new ArrayList<>(tempSize);
      
      while(tokenCollection.size() < tempSize) {
        tokenCollection.add(new HashMap<String, Integer>());
      }
      
      for (String oneLog: partitionEntry.getValue()) {
        StringTokenizer oneLogTokens = new StringTokenizer(oneLog, this.delimiter);
        ArrayList<String> logArray = new ArrayList<>(oneLogTokens.countTokens());
        
        for (int i = 0; i < tempSize; i++) {
          String oneToken = oneLogTokens.nextToken();
          logArray.add(oneToken); 
          HashMap<String, Integer> logEntry = tokenCollection.get(i);
          logEntry.put(oneToken, logEntry.containsKey(oneToken) ? (logEntry.get(oneToken) + 1) : 1);
        }
        matirxBySize.get(tempSize).add(logArray);
      }
      /* -------------------- For debugging ---------------------- */
      // printTokenCollection(tokenCollection);
      /* -------------------- For debugging ---------------------- */
      
      /*
       * Calculate the partitioning position:
       *    Reason for putting it here instead of merging it with the above for-loop:
       *    Merging with above for-loop adding lots of computation, when loop is rolling
       */
      int chosenPosition = positionCardinality(tokenCollection).getLeft();
      //out.println("Position with lowest cardinality: " + choosenPosition);
      Pair<String, Integer> tokenPosition = new Pair<>("", chosenPosition);
      
      for (ArrayList<String> logMatrix: matirxBySize.get(tempSize)) {
        String key = logMatrix.get(chosenPosition);
        ArrayList<Object> keyArray = new ArrayList<>();
        keyArray.add(tempSize);
        tokenPosition.setLeft(key);
        keyArray.add(tokenPosition);
        
        if (!partitionByPosition.containsKey(keyArray)){
          partitionByPosition.put(keyArray, new ArrayList<ArrayList<String>>());
        }
        partitionByPosition.get(keyArray).add(logMatrix);
      }

      /*
       * Check PST (Partition Support Threshold)
       */
      for (Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> subPartitionEntry: partitionByPosition.entrySet()) {
        if (subPartitionEntry.getKey().get(0) == partitionEntry.getKey()) {
          double partitionSupportRatio = (double)subPartitionEntry.getValue().size()/(double)partitionEntry.getValue().size();
          out.println(partitionSupportRatio);
        
          if (partitionSupportRatio < partitionSupportThreshold) {
            // TODO: Add lines from this partition into Outlier partition
          }
        }
      }
      
    }

    
    /* -------------------- For debugging ---------------------- */
    printPartitionsByPosition(partitionByPosition);
    //out.println(matirxBySize);
    /* -------------------- For debugging ---------------------- */

    return partitionByPosition;
    
  }
  
  
  /**
   * Print the token collections, mainly for debugging
   * @param 
   * List<HashMap<String, Integer>> tokenCollection
   */
  private void printTokenCollection(List<HashMap<String, Object>> tokenCollection) {
    for (HashMap<String, Object> logEntry: tokenCollection) {
      out.println(logEntry);
    }
  }
  
  /**
   * Print the partitions by position, mainly for debugging
   * @param 
   * Map<Integer, Map<String, ? extends Object>> partitionByPosition
   */
  @SuppressWarnings("rawtypes")
  private void printPartitionsByPosition(Map<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionByPosition) {
    for (Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> entry: partitionByPosition.entrySet()) {
      out.println(entry.getKey().get(0) + " " + ((Pair)entry.getKey().get(1)).getLeft() + " "
          + ((Pair)entry.getKey().get(1)).getRight() + " " + entry.getValue());
    }
  }
  
  
  /**
   * Determine the token position with lowest cardinality with respect to set of unique tokens
   * @param 
   * List<HashMap<String, Integer>> tokenCollection
   */
  private Pair<Integer, ArrayList<Integer>> positionCardinality(List<HashMap<String, Integer>> tokenCollection) {
    int position = 0;
    int lowestCardinality = Integer.MAX_VALUE;
    int tempSize = tokenCollection.size();
    // Keep tack of the cardinality at each position
    ArrayList<Integer> cardinality = new ArrayList<>();
    
    for (int j = 0; j < tempSize; j++) {
      int tempCardinality = tokenCollection.get(j).size();
      cardinality.add(tempCardinality);
      
      if (tempCardinality < lowestCardinality) {
        lowestCardinality = tempCardinality;
        position = j;
      } 
    }
    
    return (new Pair<Integer, ArrayList<Integer>>(position, cardinality));
  }
  
  
  /**
   * Determine the token collection information of a partition
   * TODO: need to be tested
   */
  private List<HashMap<String, Integer>> tokenCollection(Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> partition){
    
    Integer tempSize = (Integer)(partition.getKey()).get(0);
    List<HashMap<String, Integer>> tokenCollection = new ArrayList<>(tempSize);
    
    while(tokenCollection.size() < tempSize) {
      tokenCollection.add(new HashMap<String, Integer>());
    }
    
    for (ArrayList<String> logArray: partition.getValue()) {
      for (int i = 0; i < tempSize; i++) {
        String oneToken = logArray.get(i);
        HashMap<String, Integer> logEntry = tokenCollection.get(i);
        logEntry.put(oneToken, logEntry.containsKey(oneToken) ? (logEntry.get(oneToken) + 1) : 1);
      }
    }
    
    return tokenCollection;
  }
  
  
  
  /* ------------------------------------------------------------------------------------ */
  /*                          Partition by search bijection                               */
  /* ------------------------------------------------------------------------------------ */
  /**
   * partitionByTokenBijection
   * TODO: need to be tested
   */
  public void partitionByTokenBijection() {
    
    out.println("Partition by token bijection.");
    
    Map<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionByPosition = partitionByTokenPosition();
    Map<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionByBijection = new HashMap<>();
    
    for (Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionEntry: partitionByPosition.entrySet()) {
      List<HashMap<String, Integer>> tokenCollection = tokenCollection(partitionEntry);
      Pair<Integer, ArrayList<Integer>> positionCardinality = positionCardinality(tokenCollection);
      //ArrayList<ArrayList<String>> partitionIn = entry.getValue();
      //Integer tokenSize = (Integer) entry.getKey().get(0);
      Pair<Integer, Integer> tempPair = determineP1P2(partitionEntry, tokenCollection, positionCardinality);
      
      
      if (tempPair.equals(new Pair<Integer, Integer>(0, 0))) {
        // TODO: add partition to output partition
      } else {
        HashMap<String, Integer> tokensAtP1 = tokenCollection.get(tempPair.getLeft());
        HashMap<String, Integer> tokensAtP2 = tokenCollection.get(tempPair.getRight());
        Integer splitPosition = 0;
        
        for (Map.Entry<String, Integer> tokenEntry: tokensAtP1.entrySet()) {
          Integer mappingType = determineMappingType(partitionEntry, tokensAtP1, tokensAtP2);

          // TODO: change: how to determine tempTokenSet
          if (mappingType == 1) {
            /* ------------------- mapping: 1-1 ------------------- */
            splitPosition = tempPair.getLeft();
          } else if (mappingType == 2) {
            /* ------------------- mapping: 1-M ------------------- */
            HashMap<String, Integer> tempTokenSet = new HashMap<>();
            tempTokenSet = tokensAtP2;
            splitPosition = (getRankPosition(tempTokenSet, mappingType) == 1) ? tempPair.getLeft() : tempPair.getRight();
          } else if (mappingType == 3) {
            /* ------------------- mapping: M-1 ------------------- */
            HashMap<String, Integer> tempTokenSet = new HashMap<>();
            tempTokenSet = tokensAtP1;
            splitPosition = (getRankPosition(tempTokenSet, mappingType) == 2) ? tempPair.getRight() : tempPair.getLeft();
          } else if (mappingType == 4) {
            /* ------------------- mapping: M-M ------------------- */
            // TODO: create tempCollection1 and tempCollection2
          
          
          
          
          
          }
          
        }
        
        
        
        
        
        
        
        
        
        
        
        
      }

    }

  }
  

  /**
   * Determine positions P1 and P2
   * Assume Pa is before P2
   * TODO: need to be tested
   */
  private Pair<Integer, Integer> determineP1P2(Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionEntry,
                                                  List<HashMap<String, Integer>> tokenCollection, 
                                                  Pair<Integer, ArrayList<Integer>> positionCardinality) {
    Integer tokenCount = Collections.max(positionCardinality.getRight());
    
    if (tokenCount > 2) {
      Integer uniqueTokenCount = 0; 
      for (int i = 0; i < tokenCollection.size(); i++) {
        uniqueTokenCount = (tokenCollection.get(i).keySet().size() == 1) ? (uniqueTokenCount + 1) : uniqueTokenCount;
      }
      double clusterGoodness = (double)uniqueTokenCount/(double)tokenCount;
      
      if (clusterGoodness < clusterGoodnessThreshold) {
        return getMappingPositions(partitionEntry, tokenCollection, tokenCount, positionCardinality);
      } else {
        return (new Pair<Integer, Integer>(0, 0)); 
      }
    } else if (tokenCount == 2) {
      return (new Pair<Integer, Integer>(0, 1));
    } else {
      return (new Pair<Integer, Integer>(0, 0)); 
    }
  }
  
  
  /**
   * Get the mapping positions; Assume Pa is before P2
   * @param partitionIn, tokenCollection, tokenCount
   * @return Pair<Integer, Integer>
   * TODO: need to be tested
   */
  private Pair<Integer, Integer> getMappingPositions(Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionEntry, 
                                                        List<HashMap<String, Integer>> tokenCollection, Integer tokenCount, 
                                                        Pair<Integer, ArrayList<Integer>> positionCardinality) {
    Pair<Integer, Integer> tempPair = new Pair<>(0, 1);
    Integer frequentCardinality = 1;
    ArrayList<Integer> cardinality = positionCardinality.getRight();
    HashMap<Integer, Integer> cardinalityCollection = new HashMap<>();
    
    /*
     * Get the cardinality collection
     */
    for (int i = 0; i < cardinality.size(); i++) {
      Integer key = cardinality.get(i);
      cardinalityCollection.put(key, cardinalityCollection.containsKey(key) ? (cardinalityCollection.get(key) + 1) : 1);
    }
    cardinalityCollection.remove(1);
    
    /*
     * Get the frequentCardinality
     */
    Integer tempKey = 1;
    for (Map.Entry<Integer, Integer> cardinalityEntry: cardinalityCollection.entrySet()) {
      if (cardinalityEntry.getValue() > frequentCardinality) {
        tempKey = cardinalityEntry.getKey();
        frequentCardinality = cardinalityEntry.getValue();
      } else if (cardinalityEntry.getValue() == frequentCardinality) {
        /* if more than one frequent value, choose the one with lower token count*/
        tempKey = (cardinalityEntry.getKey() < tempKey) ? cardinalityEntry.getKey(): tempKey;
      }
    }
    
    /*
     * Set the P1 and P2
     */
    Integer p1 = cardinality.indexOf(tempKey);
    if (frequentCardinality > 1) {
      tempPair.setLeft(p1);
      tempPair.setRight(cardinality.subList(p1 + 1, cardinality.size()).indexOf(tempKey) + p1 +1);
    } else if (frequentCardinality == 1) {
      tempPair.setLeft(p1);
      tempPair.setRight(p1 + 1);
    }

    return tempPair;
    
  }
  
  
  /**
   * Determine the mapping type, i.e., 1-1, 1-M, M-1, or M-M
   * TODO: determine the mapping between P1 and P2
   */
  private Integer determineMappingType(Map.Entry<ArrayList<Object>, ArrayList<ArrayList<String>>> partitionEntry,
                                          HashMap<String, Integer> tokensAtP1, HashMap<String, Integer> tokensAtP2) {
    Integer mappingType = 0;
    
    
    
    
    
    
    return mappingType;
    
  }
  
  
  
  /**
   * Get rank position
   * TODO: get the position rank
   */
  private Integer getRankPosition(HashMap<String, Integer> tempTokenSet, Integer mappingType) {
    
    Integer splitRank = 0;
    Integer cardinalityOfSet = tempTokenSet.size();
    Integer linesMatchSet = 0;
    
    // TODO: determine linesMatchSet
    
    
    double distance = (double)cardinalityOfSet/(double)linesMatchSet;
    
    if (distance <= lowerBound) {
      splitRank = (mappingType == 2) ? 2 : 1;
    } else if (distance >- upperBound) {
      splitRank = (mappingType == 2) ? 1 : 2;
    } else {
      splitRank = (mappingType == 2) ? 1 : 2;
    }

    return splitRank;
  }
  
  
 
  
  
  
  
  
  
  
  


}
