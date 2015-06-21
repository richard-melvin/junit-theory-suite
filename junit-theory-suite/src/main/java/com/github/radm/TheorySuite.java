package com.github.radm;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.theories.Theory;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheorySuite extends BlockJUnit4ClassRunner {

	private static final Logger LOG = LoggerFactory
			.getLogger(TheorySuite.class);

	private TheoriesWrapper embeddedRunner;

	private List<FrameworkMethod> allMethodsWithAllArgs = null;

	private InitializationError initFail = null;

	public TheorySuite(Class<?> testClass) throws InitializationError {
		super(testClass);

		if (initFail != null)
		{
			throw initFail;
		}
	}


    @Override
    protected List<FrameworkMethod> computeTestMethods() {

         TheoriesWrapper runner = getEmbeddedRunner();

         if (runner != null && allMethodsWithAllArgs == null)
         {
        	 allMethodsWithAllArgs = new ArrayList<>();
        	 for (FrameworkMethod fm : runner.computeTestMethods())
        	 {
        		 if (fm.getAnnotation(Theory.class) == null)
        		 {
        			 allMethodsWithAllArgs.add(fm);
        		 }
        		 else
        		 {
        			 allMethodsWithAllArgs.addAll(runner.computeTestMethodsWithArgs(fm));
        		 }
        	 }
         }

         return allMethodsWithAllArgs;
    }


	@Override
	public int testCount() {
		 int size = computeTestMethods().size();
		 LOG.info("Executing {} tests", size);

		 return size;
	}


	/**
	 * Gets the embedded runner; needed as virtual methods get called from super constructor
	 *
	 * @return the embedded runner
	 */
	private TheoriesWrapper getEmbeddedRunner() {
		if (embeddedRunner == null)
		{
			try {
				embeddedRunner = new TheoriesWrapper(getTestClass().getJavaClass());
			} catch (InitializationError e) {
				initFail = e;
			}
		}

		return embeddedRunner;
	}



}
