package wtm;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.fs.FileSystem;
import org.bson.BasicBSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.MongoOutputFormat;
import com.mongodb.hadoop.io.BSONWritable;
import com.mongodb.hadoop.util.MongoConfigUtil;
import com.mongodb.DBCollection;

public class DurOverlap {
	/* 
	Object, Text : input key-value pair type (always same (to get a line of input file))
	Text, IntWritable : output key-value pair type
	*/
	public static class OverlapMapper
			extends Mapper<Object,Text,Text,Text> {
		private Text efficay = new Text();
		private Text medicine = new Text();

		
		protected void setup(Context context) throws IOException, InterruptedException {
			
		}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String [] arr = value.toString().split(","); //col별로 분할하기 5번이 효능
            		efficay.set(arr[5]);
            		medicine.set(arr[1]);
            		context.write(efficay,medicine);
		}
	}

	public static class OverlapReducer
			extends Reducer<Text,Text,Text,BSONWritable> {

		// variables
		private Text list = new Text();

		public void reduce(Text key, Iterable<Text> values, Context context) 
				throws IOException, InterruptedException {

			BasicBSONObject output = new BasicBSONObject();
			BSONWritable reduceResult = new BSONWritable();

			String s = new String();

			int comma = 0;
			for (Text val : values) {
				if (comma == 0){
					s += val.toString();
					comma = 1;
				}
				else{
                	s += "," + val.toString();
				}
			}
			/*
			list.set(s);
			context.write(key,list);
			*/

			output.put("value", s.toString());
			reduceResult.setDoc(output);
			context.write(key, reduceResult);
		}
	}


	/* Main function */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf,args).getRemainingArgs();
		if ( otherArgs.length != 2 ) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		
		/*
		FileSystem hdfs = FileSystem.get(conf);
		Path output = new Path(otherArgs[1]);
		if (hdfs.exists(output))
			hdfs.delete(output, true);
		*/

		MongoConfigUtil.setOutputURI(conf, "mongodb://j5b205.p.ssafy.io:27017/" + otherArgs[1]);
		DBCollection collection = MongoConfigUtil.getOutputCollection(conf);
		collection.drop();

		Job job = new Job(conf,"DUR overlap");
		job.setJarByClass(DurOverlap.class);

		// let hadoop know my map and reduce classes
		job.setMapperClass(OverlapMapper.class);
		job.setReducerClass(OverlapReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// set number of reduces
		job.setNumReduceTasks(2);

		// set input and output directories
		FileInputFormat.addInputPath(job,new Path(otherArgs[0]));
		// FileOutputFormat.setOutputPath(job,new Path(otherArgs[1]));
		job.setOutputFormatClass(MongoOutputFormat.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1 );
	}
}

