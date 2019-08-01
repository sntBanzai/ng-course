package pl.malyszko.jerzy.ng_linear;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSetProvider {

	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
	
	public static Set<Number[]> provide() throws IOException{
		Set<Number[]> ds = new HashSet<>();
		try (Stream<String> lines = Files.lines(Paths.get("C:\\2018_Yellow_Taxi_Trip_Data.csv"))) {
			List<String> collect = lines.skip(1).unordered().limit(Short.MAX_VALUE).collect(Collectors.toList());
			List<String[]> collect2 = collect.stream().map(str -> str.split(","))
					.map(tab -> new String[] { tab[1], tab[tab.length - 1] }).collect(Collectors.toList());
			for (String[] obj : collect2) {
				String rawDate = obj[0];
				TemporalAccessor parse = dtf.parse(rawDate);
				String totalFare = obj[1];
				Double hr = Double.parseDouble(parse.get(ChronoField.HOUR_OF_DAY) + "."
						+ (int) ((parse.get(ChronoField.MINUTE_OF_HOUR) / 60.0) * 100));
				ds.add(new Number[] { hr, Double.parseDouble(totalFare) });
			}
		}
		return ds;
	}
	
}
