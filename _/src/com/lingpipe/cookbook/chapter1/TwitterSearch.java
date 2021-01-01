package com.lingpipe.cookbook.chapter1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.lingpipe.cookbook.Util;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterSearch {

	static final int TWEETS_PER_PAGE = 100;
	static final int MAX_TWEETS = 1500;

	public static void main (String[] args) 
			throws IOException, TwitterException {
		String outFilePath = args.length > 0 ? args[0] : "data/twitterSearch.csv";
		File outFile = new File(outFilePath);
		System.out.println("Writing output to " + outFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter Twitter Query:");
		String queryString = reader.readLine();
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query(queryString + " -filter:retweets");
		query.setLang("en");
		query.setCount(TWEETS_PER_PAGE);
		query.setResultType(Query.RECENT);
		List<String[]> csvRows = new ArrayList<String[]>();
		while(csvRows.size() < MAX_TWEETS) {
			QueryResult result = twitter.search(query);
			List<Status> resultTweets = result.getTweets();
			for (Status tweetStatus : resultTweets) {
				String row[] = new String[Util.ROW_LENGTH];
				row[Util.TEXT_OFFSET] = tweetStatus.getText();
				csvRows.add(row);
			}
			System.out.println("Tweets Accumulated: " + csvRows.size());
			if ((query = result.nextQuery()) == null) {
				break;
			}
		}
		
		System.out.println("writing to disk " + csvRows.size() + " tweets at " + outFilePath);
		Util.writeCsvAddHeader(csvRows, outFile);
		
	}
}
