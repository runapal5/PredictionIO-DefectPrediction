package com.niit.defectprediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.predictionio.controller.java.P2LJavaAlgorithm;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.apache.spark.mllib.util.MLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

import org.apache.spark.rdd.RDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;



public class RandomForestAlgorithm extends P2LJavaAlgorithm<PreparedData, RandomForestModel, Query, PredictedResult> {
	
	private static final Logger logger = LoggerFactory.getLogger(RandomForestAlgorithm.class);
    private final RandomForestAlgorithmsParams ap;

    private static  List<String> predictList = new ArrayList<String>();

	public RandomForestAlgorithm(RandomForestAlgorithmsParams ap) {
        this.ap = ap;
    }
	
    

	@Override
	public PredictedResult predict(RandomForestModel randomForestModel, Query query) {		
		logger.info("**********************Prediction on the model:predict************************");
		System.out.println("**********************Prediction on the model:predict************************");
	

		final RandomForestModel model = randomForestModel;
		//logger.info("Learned classification tree model:\n" + model.toDebugString()); 

		
		
		String datapath =  ap.getTestDataFile();
		logger.info("TestData File:\n" + datapath); 
		
		
		
		String testDatas = query.getTestDataPath();
		logger.info("Test Data Path:\n" +testDatas); 
		
		//GETTING THE SPARK CONTEXT
		SparkConf conf = new SparkConf().setAppName("defectPrediction");
		JavaSparkContext ctx = JavaSparkContext.fromSparkContext(SparkContext.getOrCreate(conf));
		JavaStreamingContext context = new JavaStreamingContext(ctx, Durations.seconds(60));
		//GETTING THE SPARK CONTEXT
		
		
		 JavaRDD loadedTestdata = MLUtils.loadLibSVMFile(context.sparkContext().sc(), testDatas).toJavaRDD();  
		 logger.info("*************Test Data Path Loaded**********" +loadedTestdata.count()); 
		 
		 
		 try{
		 
			 JavaPairRDD<Double, Double> predictionAndLabel =
					 loadedTestdata.mapToPair(pf);
			 
		/*	 
		 JavaPairRDD<Double, Double> predictionAndLabel =
				 data.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
		            @Override
		            public Tuple2<Double, Double> call(LabeledPoint p) {
		            	System.out.println("Predict:::"+model.predict(p.features()) +", Actual::"+p.label());
		            	logger.info("Predict:::"+model.predict(p.features()) +", Actual::"+p.label());
		            	predictList.add(String.valueOf(p.label()));
		            	return new Tuple2<>(model.predict(p.features()), p.label());		              
		            }
		          });
		 
		 
		 
		 JavaRDD<Tuple2<Object, Object>> predictionAndLabels = data.map(
	      	      new Function<LabeledPoint, Tuple2<Object, Object>>() {
	      			public Tuple2<Object, Object> call(LabeledPoint p) {
	      	          Double prediction = model.predict(p.features());
		      	        System.out.println("Predict:::"+prediction +", Actual::"+p.label());
		            	logger.info("Predict:::"+prediction +", Actual::"+p.label());
		            	predictList.add(String.valueOf(prediction));
	      	          
	      	          return new Tuple2<Object, Object>(prediction, p.label());
	      	        }
	      	      }
	      	    ); */
		 
		 
		 }catch(Exception e){
			 logger.info("*************Exception in gettting prediction**********" +e.getMessage()); 
			 e.printStackTrace();
		 }
		 
		 logger.info("*************Prediction Done**********"+predictList.size()); 
		/*
		System.out.println("Query:Features ::::"+query.getPlan()+","+query.getRegwt()+","+query.getReqsize()+","+query.getReqquality());
		logger.info("Query:Features ::::"+query.getPlan()+","+query.getRegwt()+","+query.getReqsize()+","+query.getReqquality());
		logger.info("Predicted Result:"+randomForestModel.predict(Vectors.dense(query.getPlan(),query.getRegwt(),query.getReqsize(),query.getReqquality())));
		*/
		
		  /*List<LabeledPoint> list = new ArrayList<LabeledPoint>();
		  LabeledPoint zero = new LabeledPoint(0.0, Vectors.dense(1.0, 0.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
		  LabeledPoint one = new LabeledPoint(1.0, Vectors.dense(8.0,7.0,6.0,4.0,5.0,6.0,1.0,2.0,3.0));
		  list.add(zero);
		  list.add(one);
		  JavaRDD<LabeledPoint> data = new JavaSparkContext(qs.sparkContext()).parallelize(list);
		  */
		
        		//new PredictedResult(randomForestModel.predict(Vectors.dense(null)))
		
		return new PredictedResult(null) ;
		
	}

	@Override
	public RandomForestModel train(SparkContext sparkContext, PreparedData data) {
		logger.info("**********************Train the model************************");
		if(data != null && data.getTrainingData().getLabelledPoints() != null){
			System.out.println("Counts :::"+data.getTrainingData().getLabelledPoints().count());
			logger.info("**********************Train the model*****Counts :::"+data.getTrainingData().getLabelledPoints().count());
		}
		
		logger.info("Parmeter Values::"+ap.getNumClasses() +","+ap.getNumTrees()+","+ap.getMaxDepth());
		System.out.println("Parmeter Values::"+ap.getNumClasses() +","+ap.getNumTrees()+","+ap.getMaxDepth());
		
		HashMap<Integer, Integer> categoricalFeaturesInfo =new HashMap<Integer, Integer>();
		RandomForestModel model = RandomForest.trainClassifier(data.getTrainingData().getLabelledPoints(), ap.getNumClasses(),
			      categoricalFeaturesInfo, ap.getNumTrees(), ap.getFeatureSubsetStrategy(), ap.getImpurity(), ap.getMaxDepth(), ap.getMaxBins(),
			      ap.getSeed());
	
		
		return model;
	}
	
	 private static PairFunction<LabeledPoint, Double, Double> pf =  new PairFunction<LabeledPoint, Double, Double>() {
	        @Override
	        public Tuple2<Double, Double> call(LabeledPoint p) {
	        	logger.info("******Inside pf****");
	        	System.out.println("******Inside pf****");
	            	
	          return null;
	        }
	    };
	
	
}
