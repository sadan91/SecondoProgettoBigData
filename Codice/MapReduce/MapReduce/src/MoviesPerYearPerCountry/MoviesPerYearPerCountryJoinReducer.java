package MoviesPerYearPerCountry;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MoviesPerYearPerCountryJoinReducer extends
		Reducer<Text, Text, Text, Text> {

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		String result = "";

		for (Text value : values) {
			result = result + value.toString() + "<ENDVALUE>";
		}

		try {
			result = result.substring(0, result.length() - 10);
		} catch (Exception e) {
		}

		context.write(key, new Text(result));

	}

}