package org.bahmni.jss.registration;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.bahmni.datamigration.PatientReader;
import org.bahmni.datamigration.request.patient.*;
import org.bahmni.datamigration.session.AllPatientAttributeTypes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import static org.bahmni.jss.registration.RegistrationFields.sentenceCase;

public class AllRegistrations implements PatientReader {
    private CSVReader reader;
    private AllPatientAttributeTypes allPatientAttributeTypes;
    private Map<String, AllLookupValues> lookupValuesMap;

    public AllRegistrations(String csvLocation, String fileName, AllPatientAttributeTypes allPatientAttributeTypes, Map<String,
            AllLookupValues> lookupValuesMap) throws IOException {
        File file = new File(csvLocation, fileName);
        FileReader fileReader = new FileReader(file);

        init(allPatientAttributeTypes, lookupValuesMap, fileReader);
    }

    public AllRegistrations(AllPatientAttributeTypes allPatientAttributeTypes, Map<String, AllLookupValues> lookupValuesMap,
                            Reader reader) throws IOException {
        init(allPatientAttributeTypes, lookupValuesMap, reader);
    }

    private void init(AllPatientAttributeTypes allPatientAttributeTypes, Map<String, AllLookupValues> lookupValuesMap, Reader reader) throws IOException {
        this.lookupValuesMap = lookupValuesMap;
        this.reader = new CSVReader(reader, ',');
        this.reader.readNext(); //skip row
        this.allPatientAttributeTypes = allPatientAttributeTypes;
    }

    public PatientRequest nextPatient() throws IOException {
        String[] patientRow = reader.readNext();

        if (patientRow == null) return null;

        PatientRequest patientRequest = new PatientRequest();
        RegistrationNumber registrationNumber = RegistrationFields.parseRegistrationNumber(patientRow[0]);
        patientRequest.setPatientIdentifier(registrationNumber.getCenterCode() + registrationNumber.getId());
        patientRequest.setCenterID(new CenterId(registrationNumber.getCenterCode()));

        Name name = RegistrationFields.name(patientRow[2], patientRow[3]);
        patientRequest.setName(sentenceCase(name.getGivenName()), sentenceCase(name.getFamilyName()));

        addPatientAttribute(patientRow[4], patientRequest, "primaryRelative", null, 0);

        patientRequest.setGender(patientRow[5]);
        patientRequest.setBirthdate(RegistrationFields.getDate(patientRow[6]));
        patientRequest.setAge(Integer.parseInt(patientRow[7]));

        PatientAddress patientAddress = new PatientAddress();

        patientAddress.setCityVillage(sentenceCase(patientRow[7]));

        addPatientAttribute(patientRow[20], patientRequest, "caste", lookupValuesMap.get("Castes"), 0);
        patientAddress.setAddress3(sentenceCase(patientRow[25]));

        String district = lookupValuesMap.get("Districts").getLookUpValue(patientRow[26], 2);
        patientAddress.setCountyDistrict(sentenceCase(district));
        String state = lookupValuesMap.get("States").getLookUpValue(patientRow[26]);
        patientAddress.setStateProvince(sentenceCase(state));

        addPatientAttribute(patientRow[32], patientRequest, "class", lookupValuesMap.get("Classes"), 0);
        return patientRequest;
    }

    private void addPatientAttribute(String value, PatientRequest patientRequest, String name, LookupValueProvider lookupValueProvider, int valueIndex) {
        if (lookupValueProvider != null) {
            String lookUpValue = lookupValueProvider.getLookUpValue(value, valueIndex);
            if (lookUpValue == null) return;
        }
        if (StringUtils.isEmpty(value)) return;

        PatientAttribute patientAttribute = new PatientAttribute();
        patientAttribute.setAttributeType(allPatientAttributeTypes.getAttributeUUID(name));
        patientAttribute.setName(name);
        patientAttribute.setValue(lookupValueProvider == null ? value : sentenceCase(lookupValueProvider.getLookUpValue(value, valueIndex)));
        patientRequest.addPatientAttribute(patientAttribute);
    }

    public void done() throws IOException {
        reader.close();
    }
}