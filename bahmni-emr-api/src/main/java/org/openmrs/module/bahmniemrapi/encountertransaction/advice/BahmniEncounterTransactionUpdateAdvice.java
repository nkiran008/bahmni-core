package org.openmrs.module.bahmniemrapi.encountertransaction.advice;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.obscalculator.ObsValueCalculator;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.MethodBeforeAdvice;

import java.io.File;
import java.lang.reflect.Method;

public class BahmniEncounterTransactionUpdateAdvice implements MethodBeforeAdvice {

    private static Logger logger = Logger.getLogger(BahmniEncounterTransactionUpdateAdvice.class);
    
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        logger.info("BahmniEncounterTransactionUpdateAdvice : Start");
        GroovyClassLoader gcl = new GroovyClassLoader();
        Class clazz = gcl.parseClass(new File(OpenmrsUtil.getApplicationDataDirectory() + "obscalculator/BahmniObsValueCalculator.groovy")); 
        logger.info("BahmniEncounterTransactionUpdateAdvice : Using rules in " + clazz.getName());
        ObsValueCalculator obsValueCalculator = (ObsValueCalculator) clazz.newInstance();
        obsValueCalculator.run((BahmniEncounterTransaction) args[0]);
        logger.info("BahmniEncounterTransactionUpdateAdvice : Done");
    }
    
}
