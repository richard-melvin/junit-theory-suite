package com.github.radm.theories.runner;

import static java.util.Collections.emptyList;
import static org.javaruntype.type.Types.forJavaLangReflectType;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.ParametersSuppliedBy;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.internal.AllMembersSupplier;
import org.junit.contrib.theories.internal.BooleanSupplier;
import org.junit.contrib.theories.internal.EnumSupplier;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Work out the set of potential assignments for each argument on an individual basis.
 * Extracted from org.junit.contrib.theories.internal.Assignments, taking out the
 * fixed all-values generation logic.
 */
public class PotentialAssignmentFinder {

	private final TestClass testClass;

  private static final Logger LOG = LoggerFactory.getLogger(PotentialAssignmentFinder.class);

	public PotentialAssignmentFinder(TestClass testClass) {
		super();
		this.testClass = testClass;
	}

	public List<PotentialAssignment> potentialsFor(ParameterSignature unassigned)
			throws Throwable {
		List<PotentialAssignment> assignments = getSupplier(unassigned)
				.getValueSources(unassigned);

		if (assignments.isEmpty()) {
			assignments = generateAssignmentsFromTypeAlone(unassigned);
		}

		if (assignments.isEmpty()) {
		  LOG.warn("Unable to find any possible values for argument {}", unassigned.getName());
		}

		return assignments;
	}

	private List<PotentialAssignment> generateAssignmentsFromTypeAlone(
			ParameterSignature unassigned) {
		org.javaruntype.type.Type<?> paramType = forJavaLangReflectType(unassigned
				.getType());
		Class<?> klass = paramType.getRawClass();

		if (klass.isEnum()) {
			return new EnumSupplier(klass).getValueSources(unassigned);
		}
		if (Boolean.class.equals(klass) || boolean.class.equals(klass)) {
			return new BooleanSupplier().getValueSources(unassigned);
		}

		return emptyList();
	}

	private ParameterSupplier getSupplier(ParameterSignature unassigned)
			throws Exception {
		ParametersSuppliedBy annotation = unassigned
				.findDeepAnnotation(ParametersSuppliedBy.class);

		return annotation != null ? buildParameterSupplierFromClass(annotation
				.value()) : new AllMembersSupplier(testClass);
	}

	private ParameterSupplier buildParameterSupplierFromClass(
			Class<? extends ParameterSupplier> supplierClass) throws Exception {

		for (Constructor<?> each : supplierClass.getConstructors()) {
			Class<?>[] parameterTypes = each.getParameterTypes();
			if (parameterTypes.length == 1
					&& TestClass.class.equals(parameterTypes[0])) {
				return (ParameterSupplier) each.newInstance(testClass);
			}
		}

		return supplierClass.newInstance();
	}

}
