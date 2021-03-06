package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml", "classpath:webModuleApplicationContext.xml"}, inheritLocations = true)
public class VisitDaoImplIT extends BaseContextSensitiveTest {
    
    @Autowired
    VisitDao visitDao;

    @Autowired
    PatientDao patientDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitTestData.xml");
    }

    @Test
    public void shouldGetLatestObsForConceptSetByVisit() {
        Visit latestVisit = visitDao.getLatestVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Weight");
        assertEquals(901, latestVisit.getVisitId().intValue());
    }

    @Test
    public void shouldGetVisitsByPatient(){
        Patient patient = patientDao.getPatient("GAN200000");
        List<Visit> visits = visitDao.getVisitsByPatient(patient, 1);
        assertEquals(1, visits.size());
        assertEquals(901, visits.get(0).getVisitId().intValue());
    }

    @Test
    public void shouldNotGetVoidedEncounter() throws Exception {
        List<Encounter> admitAndDischargeEncounters = visitDao.getAdmitAndDischargeEncounters(902);
        assertEquals(1, admitAndDischargeEncounters.size());
    }
}