package com.example.patientmanagement.service;

import com.example.patientmanagement.domain.Doctor;
import com.example.patientmanagement.exception.ResourceNotFoundException;
import com.example.patientmanagement.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor("Jane", "Smith", "Cardiology", "1112223333", "jane.smith@example.com");
        doctor.setId(1L);
    }

    @Test
    void testCreateDoctor() {
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        Doctor createdDoctor = doctorService.createDoctor(doctor);

        assertNotNull(createdDoctor);
        assertEquals(doctor.getFirstName(), createdDoctor.getFirstName());
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void testGetDoctorById_Found() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        Optional<Doctor> foundDoctorOptional = doctorService.getDoctorById(1L);

        assertTrue(foundDoctorOptional.isPresent());
        Doctor foundDoctor = foundDoctorOptional.get();
        assertEquals(doctor.getId(), foundDoctor.getId());
        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDoctorById_NotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getDoctorById(1L);
        });

        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateDoctor_Success() {
        Doctor updatedDetails = new Doctor("Jane", "Smith", "Neurology", "3332221111", "jane.s.new@example.com");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        Doctor updatedDoctor = doctorService.updateDoctor(1L, updatedDetails);

        assertNotNull(updatedDoctor);
        assertEquals(updatedDetails.getSpecialization(), updatedDoctor.getSpecialization());
        assertEquals(updatedDetails.getPhoneNumber(), updatedDoctor.getPhoneNumber());
        verify(doctorRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void testUpdateDoctor_NotFound() {
        Doctor updatedDetails = new Doctor("John", "Doe", "Pediatrics", "1231231234", "john.d@example.com");
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.updateDoctor(1L, updatedDetails);
        });

        verify(doctorRepository, times(1)).findById(1L);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void testDeleteDoctor_Success() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(doctorRepository).deleteById(1L);

        assertDoesNotThrow(() -> doctorService.deleteDoctor(1L));

        verify(doctorRepository, times(1)).existsById(1L);
        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteDoctor_NotFound() {
        when(doctorRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.deleteDoctor(1L);
        });

        verify(doctorRepository, times(1)).existsById(1L);
        verify(doctorRepository, never()).deleteById(1L);
    }
}
