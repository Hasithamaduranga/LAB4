package com.example.patientmanagement.service;

import com.example.patientmanagement.domain.Patient;
import com.example.patientmanagement.exception.ResourceNotFoundException;
import com.example.patientmanagement.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient("John", "Doe", LocalDate.of(1990, 1, 1), "1234567890", "john.doe@example.com", "123 Main St");
        patient.setId(1L);
    }

    @Test
    void testCreatePatient() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient createdPatient = patientService.createPatient(patient);

        assertNotNull(createdPatient);
        assertEquals(patient.getFirstName(), createdPatient.getFirstName());
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void testGetPatientById_Found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Optional<Patient> foundPatientOptional = patientService.getPatientById(1L);

        assertTrue(foundPatientOptional.isPresent());
        Patient foundPatient = foundPatientOptional.get();
        assertEquals(patient.getId(), foundPatient.getId());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPatientById_NotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getPatientById(1L);
        });

        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdatePatient_Success() {
        Patient updatedDetails = new Patient("John", "Doe", LocalDate.of(1990, 1, 1), "0987654321", "john.doe.new@example.com", "456 Main St");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient); // patient object is updated in service

        Patient updatedPatient = patientService.updatePatient(1L, updatedDetails);

        assertNotNull(updatedPatient);
        assertEquals(updatedDetails.getPhoneNumber(), updatedPatient.getPhoneNumber());
        assertEquals(updatedDetails.getEmail(), updatedPatient.getEmail());
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void testUpdatePatient_NotFound() {
        Patient updatedDetails = new Patient("Jane", "Doe", LocalDate.of(1990, 1, 1), "0987654321", "jane.doe@example.com", "456 Oak St");
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updatePatient(1L, updatedDetails);
        });

        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testDeletePatient_Success() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(1L);

        assertDoesNotThrow(() -> patientService.deletePatient(1L));

        verify(patientRepository, times(1)).existsById(1L);
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletePatient_NotFound() {
        when(patientRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.deletePatient(1L);
        });

        verify(patientRepository, times(1)).existsById(1L);
        verify(patientRepository, never()).deleteById(1L);
    }
}
