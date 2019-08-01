package pl.malyszko.jerzy.ng_linear;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinearRegressionWithOneVariable {


	public static void main(String[] args) throws IOException, ParseException {
//		List<Number[]> ds = Stream
//				.of(new Number[] { 1, 1 }, new Number[] { 2, 2.5 }, new Number[] { 3, 4 }, new Number[] { 10, 8 })
//				.collect(Collectors.toList());
		Set<Number[]> ds = DataSetProvider.provide();
		final double mean = ds.stream().mapToDouble(ntab -> ntab[0].doubleValue()).reduce(0.0, (n1, n2) -> n1 + n2)
				/ ds.size();
		final double variation = ds.stream().mapToDouble(ntab -> ntab[0].doubleValue() - mean)
				.map(db -> Math.pow(db, 2)).reduce(0, (db1, db2) -> db1 + db2) / (ds.size() - 1);
		final double stdev = Math.sqrt(variation);
		ds.forEach(tab -> tab[0] = (tab[0].doubleValue() - mean) / stdev);
		BiFunction<Number, Number, Number> costFunction = new CostFunction(ds);
		Number currLoss = null;
		Number prevLoss = null;
		Number theta0 = 0;
		Number theta1 = 1;
		BiFunction<Number, Number, Number[]> grad = new GradientDescent(1.75, ds);
		for (;;) {
			prevLoss = currLoss;
			currLoss = costFunction.apply(theta0, theta1);
			System.out.println(theta0 + ", " + theta1 + " = " + currLoss);
			if (currLoss.doubleValue() == 0.0)
				break;
			if (prevLoss != null && prevLoss.doubleValue() <= currLoss.doubleValue()) {
				break;
			}
			Number[] feedback = grad.apply(theta0, theta1);
			if (theta0 == feedback[0] && theta1 == feedback[1])
				break;
			theta0 = feedback[0];
			theta1 = feedback[1];
		}

//		System.out.println(costFunction.apply(0, 0.5));
//		System.out.println(costFunction.apply(0, 0.25));
//		System.out.println(costFunction.apply(5, -3));
	}

}

class Hypothesis implements BinaryOperator<Number> {

	private final Number x;

	public Hypothesis(Number x) {
		this.x = x;
	}

	@Override
	public Number apply(Number theta0, Number theta1) {
		return BigDecimal.valueOf(theta0.doubleValue())
				.add(BigDecimal.valueOf(theta1.doubleValue()).multiply(BigDecimal.valueOf(x.doubleValue())));
	}

}

class CostFunction implements BinaryOperator<Number> {

	private final Collection<Number[]> dataSet;

	public CostFunction(Collection<Number[]> dataSet) {
		if (dataSet == null)
			throw new IllegalStateException();
		this.dataSet = dataSet;
	}

	@Override
	public Number apply(Number theta0, Number theta1) {
		Stream<Number> s = dataSet.stream().map(ent -> new Hypothesis(ent[0])
				.andThen(hx -> hx.doubleValue() - ent[1].doubleValue()).apply(theta0, theta1));
		Double halfWayGo = s.map(n -> Math.pow(n.doubleValue(), 2)).reduce((d1, d2) -> d1 + d2).get();
		return 1.0 / (2.0 * dataSet.size()) * halfWayGo;
	}

}

class GradientDescent implements BiFunction<Number, Number, Number[]> {

	private final double learningRate;
	private final Collection<Number[]> dataSet;

	public GradientDescent(double learningRate, Collection<Number[]> dataSet) {
		this.learningRate = learningRate;
		this.dataSet = dataSet;
	}

	@Override
	public Number[] apply(Number theta0, Number theta1) {
		Number theta0Loc = theta0.doubleValue();
		Number theta1Loc = theta1.doubleValue();
		theta0 = theta0.doubleValue()
				- (learningRate * (dataSet.stream().map(ent -> hype(ent).apply(theta0Loc, theta1Loc))
						.reduce((n1, n2) -> n1.doubleValue() + n2.doubleValue()).get() / dataSet.size()));
		theta1 = theta1.doubleValue()
				- (learningRate * (dataSet.stream()
						.map(ent -> hype(ent).andThen(hx -> hx.doubleValue() * ent[0].doubleValue()).apply(theta0Loc,
								theta1Loc))
						.reduce((n1, n2) -> n1.doubleValue() + n2.doubleValue()).get() / dataSet.size()));
		return new Number[] { theta0, theta1 };
	}

	private BiFunction<Number, Number, Double> hype(Number[] ent) {
		return new Hypothesis(ent[0]).andThen(hx -> hx.doubleValue() - ent[1].doubleValue());
	}

}
