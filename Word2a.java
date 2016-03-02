package Package1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
//import java.util.Set;
import java.util.StringTokenizer;
//import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
//import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Word2a {
	public static class BiMapper extends Mapper<Object, Text, Text, Text> {
		private Text word = new Text();
		private String year = new String("####");
		private String author = new String("");

		private boolean readbook;
		

		private String start = new String("_START_");
//		public String end = new String("_END_");

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			String line = value.toString().toLowerCase();
			
			byte[] fileContent = value.getBytes();
			BufferedReader bufReader = new BufferedReader(new StringReader(
					new String(fileContent)));

			while ((line = bufReader.readLine()) != null) {
			if (readbook) {

				while (itr.hasMoreTokens()) {
					String s = itr.nextToken().replaceAll("[^a-zA-Z]", "").toLowerCase();

					if((s.length() != 0) && (!year.equals("####"))) {
						String s1 = start + "  " + s;
						word.set(s1 + "\t" + year);
						context.write(word, new Text(author));
						start = s;
					}

				}
				word.set(start + "\t" + "_END_" + "\t" + year);
				context.write(word, new Text(author));
			}

			
			else if (line.toLowerCase().startsWith("*** start of")) {
				readbook = true;
			} 

			else if (line.toLowerCase().startsWith("release date:")) {
				int startyear = line.indexOf(",") + 2;
				year = line.substring(startyear, startyear + 4);

			} 
			
			else if (line.toLowerCase().startsWith("author:")) {
				author = line.substring(line.lastIndexOf(" ") + 1);

			}

		}

	}
	}

	public static class BiReducer extends Reducer<Text, Text, Text, Text> {
		

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
//			int sum = 0;
//			Set<String> s = new TreeSet<String>();
			
			for (Text val : values) {
				context.write(key,val);
//				s.add(val.toString());
//				sum += 1;
			}

//			context.write(key, new Text(sum + "\t" + s.size()));

		}
	}

	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(Word2a.class);
		
		job.setInputFormatClass(WholeFileInputFormat.class);
		job.setMapperClass(BiMapper.class);
//		job.setCombinerClass(BiReducer.class);
		
		job.setReducerClass(BiReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		WholeFileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
//		FileInputFormat.addInputPath(job, new Path(args[0]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}