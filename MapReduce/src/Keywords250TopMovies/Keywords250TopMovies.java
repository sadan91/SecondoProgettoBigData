package Keywords250TopMovies;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Keywords250TopMovies {

	private static final String OUTPUT_PATH = "/intermediate_output_keyword";

	public static void main(String[] args) throws ClassNotFoundException,
	IOException, InterruptedException {

		if (args.length != 3) {
			System.err.println("USAGE: <hdfs keywords path> <hdfs top250movies path> <hdfs output path>");
			System.exit(1);
		}

		/* Primo Job: esegue il join tra i due file in input */
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Keywords250TopMovies_join");

		job.setJarByClass(Keywords250TopMovies.class);
		job.setMapperClass(Keywords250TopMoviesJoinMapper.class);

		job.setReducerClass(Keywords250TopMoviesJoinReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		try {
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileInputFormat.addInputPath(job, new Path(args[1]));
		} catch (Exception e) {
			System.err.println("Error opening input path");
		}
		FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH));

		job.waitForCompletion(true);
		
		
		
		/* Secondo Job: conta i film per ciascuna keyword */
		Configuration conf2 = new Configuration();
		Job job2 = Job.getInstance(conf2, "Keywords250TopMovies_count");

		job2.setJarByClass(Keywords250TopMovies.class);
		job2.setMapperClass(Keywords250TopMoviesCountMapper.class);

		job2.setNumReduceTasks(1);
		job2.setReducerClass(Keywords250TopMoviesCountReducer.class);

		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(IntWritable.class);

		try {
			FileInputFormat.addInputPath(job2, new Path(OUTPUT_PATH));
		} catch (Exception e) {
			System.err.println("Error opening input path");
		}
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));

		System.exit(job2.waitForCompletion(true) ? 0 : 1);
	}
}