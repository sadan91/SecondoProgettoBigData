package Countries250TopMovies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Il mapper costruisce le coppie (nazione, 1) se il film è nei top 250 e le
 * invia al reducer
 *
 */
public class Countries250TopMoviesMapper extends
		Mapper<Object, Text, Text, IntWritable> {

	/*
	 * Il Set è necessario per evitare di scrivere più volte la stesso film per
	 * la stessa nazione (dovuto a matching multipli della stessa sottostringa)
	 */
	private static Set<String> movieNationPairs = new HashSet<String>();

	private List<String> top250movies = null;

	protected void setup(Context context) throws IOException {

		if (top250movies == null) {

			top250movies = new ArrayList<String>();

			// Path del file in HDFS
			Path path = new Path("/input/top250movies.list");
			FileSystem fs = FileSystem.get(new Configuration());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fs.open(path)));
			String line = br.readLine();

			while (line != null) {
				String[] values = line.split("\t");
				top250movies.add(values[3]);
				line = br.readLine();
			}
		}

	}

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {

		/* Per prima cosa divido l'input in maniera opportuna */

		String[] values = value.toString().split("\t");

		/* Controllo se il film è nei top 250 */
		boolean found = false;
		int listSize = top250movies.size();

		for (int i = 0; i < listSize && !found; i++) {
			if (values[0].equals(top250movies.get(i))
					&& movieNationPairs.add(top250movies.get(i) + values[1])) {

				found = true;
				context.write(new Text(values[1]), new IntWritable(1));
			}
		}

	}

}