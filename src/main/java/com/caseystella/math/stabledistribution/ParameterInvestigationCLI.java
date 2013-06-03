package com.caseystella.math.stabledistribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;

import com.caseystella.KNN;
import com.caseystella.KNN.Payload;
import com.caseystella.KNN.Result;
import com.caseystella.interfaces.IDistanceMetric;
import com.caseystella.lsh.L1LSH;
import com.caseystella.lsh.L2LSH;
import com.caseystella.util.InMemoryBackingStore;
import com.google.common.base.Splitter;



public class ParameterInvestigationCLI 
{
	public static enum LSH
	{
		L1
	  , L2
	}
	
	public static double QUERY_SIZE = 0.7;
	
	/**
	 * This is currently pretty dumb, make it less dumb in the future.
	 * 
	 * @param vectorFile
	 * @return
	 * @throws IOException 
	 */
	private static List<RealVector> loadVectors(BufferedReader br) throws IOException
	{
		List<RealVector> ret = new ArrayList<RealVector>();
		for(String line = null; (line = br.readLine()) != null;)
		{
			ArrayList<Double> doubles = new ArrayList<Double>();
			for (String token : Splitter.on(' ')
										.trimResults()
										.split(line)
				)
			{
				if(token.length() > 0)
				{
					doubles.add(Double.parseDouble(token));
				}
			}
			double[] dup = new double[doubles.size()];
			int i = 0;
			for(Double d : doubles)
			{
				dup[i++] = d;
			}
			RealVector vec = new ArrayRealVector(dup);
			ret.add(vec);
		}
		return ret;
	}
	
	private static class Stats
	{
		double averagePercentage;
		double averageNumSeen;
		double averageSizeReturned;
	}
	
	private static Stats driver( float hashWidth
								, int numHashes
								, double r
								, LSH lshFunction
								, List<RealVector> vectors
								) throws MathException
	{
		int dimension = vectors.get(0).getDimension();
		KNN knn = null;
		if(lshFunction == LSH.L1)
		{
			knn = new KNN( numHashes
					 	 , dimension
					 	 , 0
					 	 , new L1LSH.Creator(3, hashWidth)
					 	 , new InMemoryBackingStore()
						 );
		}
		else if(lshFunction == LSH.L2)
		{
			knn = new KNN( numHashes
				 	 , dimension
				 	 , 0
				 	 , new L2LSH.Creator(3, hashWidth)
				 	 , new InMemoryBackingStore()
					 );
		}
		for(RealVector vector : vectors)
		{
			knn.insert( new Payload(vector, new byte[] {}));
		}
		Stats stats = new Stats();
		double sum = 0.0d;
		int n = 0;
		for(RealVector query : vectors)
		{
	    	Result results = knn.query(query, r);
	    	
	    	int hypothesisPayloadSize = 0;
	    	for(Payload result : results.getPayloads())
	    	{
	    		hypothesisPayloadSize++;
	    		
	    	}
	    	stats.averageSizeReturned += hypothesisPayloadSize;
	    	int actualNum = 0;
	    	for(RealVector v : vectors)
	    	{
	    		double distance = knn.getUnderlyingMetric().apply(v, query);
	    		if(distance < r)
	    		{
	    			actualNum++;
	    		}
	    	}
	    	stats.averageNumSeen += results.getTotalItemsReturned() / actualNum;
	    	sum += 100.0*hypothesisPayloadSize/actualNum;
	    	++n;
	    	
		}
		stats.averagePercentage = sum/n;
		stats.averageNumSeen /= n;
		stats.averageSizeReturned /= n;
		return stats;
	}
	
	private static List<String> execute( int numHashesStart
							  , int numHashesEnd
							  , double probability
							  , double hashWidthStart
							  , double hashWidthEnd
							  , double deltaStep
							  , LSH lshFunction
							  , List<RealVector> vectors
							  , double queryRadius
							  ) throws MathException
	{
		List<String> ret = new ArrayList<String>();
		int totalNum = (int)((numHashesEnd - numHashesStart) * Math.ceil((hashWidthEnd - hashWidthStart)/deltaStep));
		int onePercent = totalNum/100;
		int tenPercent = onePercent*10;
		int i = 0;
		IDistanceMetric distanceMetric = null;
		if(lshFunction == LSH.L1)
		{
			distanceMetric = L1LSH.metric;
		}
		else if(lshFunction == LSH.L2)
		{
			distanceMetric = L2LSH.metric;
		}
		double r = queryRadius;
		if(r == 0)
		{
			double maxWidth = 0;
			for(RealVector vector : vectors)
			{
				for(RealVector vector2 : vectors)
				{
					double distance = distanceMetric.apply(vector, vector2);
					if(distance > maxWidth)
					{
						maxWidth = distance;
					}
				}
			}
			
			r= maxWidth * QUERY_SIZE;
			System.out.println("Max width: " + maxWidth + ", setting r to " + r);
		}
		for(int hashes = numHashesStart;hashes < numHashesEnd; ++hashes)
		{
			for(double hashWidth = hashWidthStart;hashWidth < hashWidthEnd;hashWidth += deltaStep)
			{
				Stats stats = driver((float)hashWidth, hashes, r, lshFunction, vectors);
				++i;
				
				if(tenPercent > 0 && i % tenPercent == 0)
				{
					System.out.println(" -- " + 100.0*i/totalNum);
				}
				else if(onePercent > 0 && i % onePercent == 0)
				{
					System.out.print(".");
				}
				if(stats.averagePercentage > probability)
				{
					ret.add( "hit rate=" + stats.averagePercentage + ", average size returned = " + stats.averageSizeReturned + ", average items seen = " + stats.averageNumSeen + ", width=" + hashWidth + ", numHashes=" + hashes);
				}
			}
		}
		return ret;
	}
	
	public static void main(String... argv) throws FileNotFoundException, IOException, MathException
	{
		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		options.addOption("i", "input_vectors", true, "Input Vectors"); 
		options.addOption("l", "lsh", true, "LSH hash function to use (options are L1 or L2)" );
		options.addOption("s", "num_hashes_start", true, "Number of hash functions to start with" );
		options.addOption( "e", "num_hashes_end", true,"Number of hash functions to end" );
		
		options.addOption( "p", "probability", true, "Target probability");
				
		options.addOption( "r", "hash_width_start", true, "Hash Width Start");
				
		options.addOption( "t", "hash_width_end", true, "Hash Width End");
				
		options.addOption( "d", "delta_step", true, "Delta Step");
		options.addOption( "q", "query_radius", true, "Query Radius");	
		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, argv );
		    
		    int numHashesStart = Integer.parseInt(line.getOptionValue('s'));
			int numHashesEnd = Integer.parseInt(line.getOptionValue("num_hashes_end"));
			double probability = Double.parseDouble(line.getOptionValue("probability"));
			double hashWidthStart = Double.parseDouble(line.getOptionValue("hash_width_start"));
			double hashWidthEnd = Double.parseDouble(line.getOptionValue("hash_width_end"));;
			double deltaStep = line.hasOption("delta_step")?Double.parseDouble(line.getOptionValue("delta_step")):0.1;
			double queryRadius = Double.parseDouble(line.getOptionValue("query_radius"));
			LSH lshHash = LSH.valueOf(line.getOptionValue("lsh"));
			File inputFile = new File(line.getOptionValue("input_vectors"));
			if(probability < 1)
			{
				probability = 100*probability;
			}
			for(String tokens :
			execute( numHashesStart
				   , numHashesEnd
				   , probability
				   , hashWidthStart
				   , hashWidthEnd
				   , deltaStep
				   , lshHash
				   , loadVectors(new BufferedReader(new FileReader(inputFile)))
				   , queryRadius
				   )
			   )
			{
				System.out.println(tokens);
			}
			
		}
		catch( Throwable exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		    exp.printStackTrace(System.err);
		 // automatically generate the help statement
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp( "parameter_estimator", options );
		}
	}
}
