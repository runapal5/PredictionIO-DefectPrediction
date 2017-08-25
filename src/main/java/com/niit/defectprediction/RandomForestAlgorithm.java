package com.niit.defectprediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.predictionio.controller.java.P2LJavaAlgorithm;
import org.apache.predictionio.data.storage.Model;
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
import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;


public class RandomForestAlgorithm extends P2LJavaAlgorithm<PreparedData, RandomForestModel, Query, PredictedResult> {
	
	private static final Logger logger = LoggerFactory.getLogger(RandomForestAlgorithm.class);
    private final RandomForestAlgorithmsParams ap;

    private SparkContext sparkContext;
    
    public SparkContext getSparkContext() {
		return sparkContext;
	}

	public void setSparkContext(SparkContext sparkContext) {
		this.sparkContext = sparkContext;
	}

	public RandomForestAlgorithm(RandomForestAlgorithmsParams ap) {
        this.ap = ap;
    }
	
    

	@Override
	public PredictedResult predict(RandomForestModel randomForestModel, Query query) {		
		logger.info("**********************Prediction on the model:predict************************");
		System.out.println("**********************Prediction on the model:predict************************");
	

		final RandomForestModel model = randomForestModel;
		logger.info("Learned classification tree model:\n" + model.toDebugString()); 

		

		String datapath =  ap.getTestDataFile();
		logger.info("TestData File:\n" + datapath); 
		
		
		logger.info("Spark Context:\n" + getSparkContext()); 
		
		SparkSession spark =  SparkSession.builder().master("local[*]").appName("defectPrediction").config("spark.sql.warehouse.dir", "file:////quickstartapp/DefectPrediction/target/").getOrCreate();
		logger.info("New Spark Context:\n" + spark.sparkContext()); 
		
		
		String testDatas = query.getTestDataPath();
		logger.info("Test Data Path:\n" +testDatas); 
		
		
		
		
		
		 JavaRDD data = MLUtils.loadLibSVMFile(getSparkContext(), testDatas).toJavaRDD();
		  
		 JavaPairRDD<Double, Double> predictionAndLabel =
				 data.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
		            @Override
		            public Tuple2<Double, Double> call(LabeledPoint p) {
		                //System.out.println("Label:::"+p.label() +", Features::"+p.features());
		            	logger.info("Predict:::"+model.predict(p.features()) +", Actual::"+p.label());
						double[] featArr = p.features().toArray();
		              
		            	return new Tuple2<>(model.predict(p.features()), p.label());
		              
		            }
		          });  
		 
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
		setSparkContext(sparkContext);
		
		return model;
	}
	
	
	
	
}
