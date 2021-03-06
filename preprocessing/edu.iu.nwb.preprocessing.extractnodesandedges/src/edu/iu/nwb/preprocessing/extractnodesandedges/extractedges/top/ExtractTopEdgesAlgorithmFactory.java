package edu.iu.nwb.preprocessing.extractnodesandedges.extractedges.top;

import java.util.Dictionary;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmFactory;
import org.cishell.framework.algorithm.ParameterMutator;
import org.cishell.framework.data.Data;
import org.osgi.service.metatype.ObjectClassDefinition;

import edu.iu.nwb.preprocessing.extractnodesandedges.util.NumericAttributeFinder;

public class ExtractTopEdgesAlgorithmFactory implements AlgorithmFactory,
		ParameterMutator {
	public Algorithm createAlgorithm(Data[] data,
			Dictionary<String, Object> parameters, CIShellContext context) {
		return new ExtractTopEdgesAlgorithm(data, parameters, context);
	}

	public ObjectClassDefinition mutateParameters(Data[] data,
			ObjectClassDefinition parameters) {
		return NumericAttributeFinder.mutateParametersForEdges(data, parameters);
	}

}