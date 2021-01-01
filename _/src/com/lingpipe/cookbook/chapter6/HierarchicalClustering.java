package com.lingpipe.cookbook.chapter6;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.cluster.AbstractHierarchicalClusterer;
import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.spell.EditDistance;
import com.aliasi.util.Distance;

public class HierarchicalClustering {

	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		boolean allowTranspositions = false;
		Distance<CharSequence> editDistance = new EditDistance(allowTranspositions);

		Set<String> inputSet = new HashSet<String>();
		String [] input = { "aa", "aaa", "aaaaa", "bbb", "bbbb" };
		inputSet.addAll(Arrays.asList(input));

		AbstractHierarchicalClusterer<String> slClusterer 
			= new SingleLinkClusterer<String>(editDistance);

		Dendrogram<String> slDendrogram
			= slClusterer.hierarchicalCluster(inputSet);


		System.out.println("\nSingle Link Dendrogram");
		System.out.println(slDendrogram.prettyPrint());

		AbstractHierarchicalClusterer<String> clClusterer 
			= new CompleteLinkClusterer<String>(editDistance); 

		Dendrogram<String> clDendrogram 
				= clClusterer.hierarchicalCluster(inputSet);
	
		System.out.println("\nComplete Link Dendrogram");
		System.out.println(clDendrogram.prettyPrint());

		System.out.println("\nSingle Link Clusterings with k Clusters");		
		for (int k = 1; k < 6; ++k ) {
			Set<Set<String>> slKClustering = slDendrogram.partitionK(k);
			System.out.println(k + "  " + slKClustering);
		}
		
		Set<Set<String>> slClustering= slClusterer.cluster(inputSet);
		System.out.println("\nComplete Link Clustering No Max Distance");
		System.out.println(slClustering);
		System.out.println("\n");

		for (int k = 1; k < 6; ++k ) {
			clClusterer.setMaxDistance(k);
			System.out.println("Complete Link Clustering at Max Distance= " + k);
			Set<Set<String>> slClusteringMd= clClusterer.cluster(inputSet);
			System.out.println(slClusteringMd);
		}
	}

}
