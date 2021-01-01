package com.lingpipe.cookbook.chapter6;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.aliasi.cluster.AbstractHierarchicalClusterer;
import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.io.FileLineReader;
import com.aliasi.spell.EditDistance;
import com.aliasi.spell.JaccardDistance;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.Distance;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

public class SingleLinkClusteringEditDistance {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		int TEXT_INDEX = 0;
		Distance<CharSequence> EDIT_DISTANCE = new EditDistance(false);
		
		//Distance<CharSequence> EDIT_DISTANCE = new JaccardDistance(CharacterTokenizerFactory.INSTANCE);
		Distance<CharSequence> TFIDF_DISTANCE = new TfIdfDistance(IndoEuropeanTokenizerFactory.INSTANCE);
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream("data/city_populations_2012.csv"),Strings.UTF8));
		List<String[]> lines = csvReader.readAll();
		int lineCount = 0;
		Set<String> inputSet = new HashSet<String>();
		for(String[] line : lines){
			if(lineCount++ < 1) continue; //Skip header
			//if(lineCount > 100) continue;
			String text = line[TEXT_INDEX];
			inputSet.add(text);
			//System.out.println(text);
			
			
		}
		/*
		inputSet = new HashSet<String>();
		//String [] input = {"aa","aaaa","bbbb","bb","abc","aabbcc"};
		String [] input = { "aa", "aaa", "aaaaa", "bbb", "bbbb" };
		//String [] input = {"a","aaa","aaaaaa","aaaaaaaa","aaaaaaaaaa"};
		inputSet.addAll(Arrays.asList(input));
		*/
		// Single-Link Clusterer
		double maxDistance = 4;
        HierarchicalClusterer<String> slClusterer 
            = new SingleLinkClusterer<String>(maxDistance,
                                              EDIT_DISTANCE);
        
        
        
     // Hierarchical Clustering
        Dendrogram<String> slDendrogram
            = slClusterer.hierarchicalCluster(inputSet);
        //System.out.println("\nSingle Link Dendrogram");
        //System.out.println(slDendrogram.prettyPrint());
       
        
        System.out.println("\nSingle Link Clusterings with k Clusters");
        //int [] kValues = {2,10,50,100};
        int [] kValues = {1,2,3,4,5};
        for (int k:kValues) {
            Set<Set<String>> slKClustering = slDendrogram.partitionK(k);
            System.out.println(k + "  " + slKClustering);
        }
        
        Set<Set<String>> slClustering= slClusterer.cluster(inputSet);
        System.out.println("\nSingle Link Clustering");
        System.out.println(slClustering);
        System.out.println("\n");
        
        for(int k: kValues){
        	((AbstractHierarchicalClusterer<String>) slClusterer).setMaxDistance(k);
        	System.out.println("Single Link Clustering at Max Distance= "+k);
        	Set<Set<String>> slClusteringMd= slClusterer.cluster(inputSet);
            //System.out.println("\nSingle Link Clustering");
            System.out.println(slClusteringMd);
            //Dendrogram<String> slDendrogramMd = slClusterer.hierarchicalCluster(inputSet);
            //System.out.println(slDendrogramMd.prettyPrint());
        }
        
        ObjectToCounterMap<Set<String>> clusterCounterMap = new ObjectToCounterMap<Set<String>>();
        for(Set<String> ss : slClustering){
        	//System.out.println("Cluster #: " + clusterCount + " ItemsInCluster: " + ss.size() + ":" + ss.toString());
        	
        	clusterCounterMap.set(ss, ss.size());
        }
        
        int clusterCount = 0;
        System.out.println("\nSingle Link Clustering with more than two items in cluster");
		for(Set<String> ss : clusterCounterMap.keysOrderedByCountList()){
			if (ss.size() == 1) continue;
			System.out.println("Cluster #: " + clusterCount + " ItemsInCluster: " + ss.size() + ":" + ss.toString());
			clusterCount++;
		}
		
		
	}

}
