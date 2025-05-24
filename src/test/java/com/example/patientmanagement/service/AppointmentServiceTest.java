package com.example.patientmanagement.service;

import com.example.patientmanagement.domain.Appointment;
import com.example.patientmanagement.domain.Doctor;
import com.example.patientmanagement.domain.Patient;
import com.example.patientmanagement.exception.ResourceNotFoundException;
import com.example.patientmanagement.repository.AppointmentRepository;
import com.example.patientmanagement.repository.DoctorRepository;
import com.example.patientmanagement.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = new Patient("John", "Doe", LocalDate.of(1990, 1, 1), "1234567890", "john.doe@example.com", "123 Main St");
        patient.setId(1L);

        doctor = new Doctor("Jane", "Smith", "Cardiology", "1112223333", "jane.smith@example.com");
        doctor.setId(1L);

        appointment = new Appointment(LocalDateTime.now(), "Regular Checkup", "SCHEDULED", patient, doctor);
        appointment.setId(1L);
    }

    @Test
    void testCreateAppointment_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Appointment newAppointment = new Appointment(LocalDateTime.now(), "Regular Checkup", "SCHEDULED", null, null);
        Appointment createdAppointment = appointmentService.createAppointment(newAppointment, 1L, 1L);

        assertNotNull(createdAppointment);
        assertEquals(patient.getId(), createdAppointment.getPatient().getId());
        assertEquals(doctor.getId(), createdAppointment.getDoctor().getId());
        assertEquals("Regular Checkup", createdAppointment.getReason());
        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).findById(1L);
        verify(appointmentRepository, times(1)).save(newAppointment);
    }

    @Test
    void testCreateAppointment_PatientNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        // No need to mock doctorRepository if patient is not found first

        Appointment newAppointment = new Appointment(LocalDateTime.now(), "Regular Checkup", "SCHEDULED", null, null);

        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(newAppointment, 1L, 1L);
        });

        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, never()).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_DoctorNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        Appointment newAppointment = new Appointment(LocalDateTime.now(), "Regular Checkup", "SCHEDULED", null, null);

        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(newAppointment, 1L, 1L);
        });

        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).findById(1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }


    @Test
    void testGetAppointmentById_Found() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        Optional<Appointment> foundAppointmentOptional = appointmentService.getAppointmentById(1L);

        assertTrue(foundAppointmentOptional.isPresent());
        Appointment foundAppointment = foundAppointmentOptional.get();
        assertEquals(appointment.getId(), foundAppointment.getId());
        verify(appointmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAppointmentById_NotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.getAppointmentById(1L);
        });

        verify(appointmentRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateAppointmentStatus_Success() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        String newStatus = "COMPLETED";
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(1L, newStatus);

        assertNotNull(updatedAppointment);
        assertEquals(newStatus, updatedAppointment.getStatus());
        verify(appointmentRepository, times(1)).findById(1L);
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void testUpdateAppointmentStatus_NotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        String newStatus = "COMPLETED";
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.updateAppointmentStatus(1L, newStatus);
        });

        verify(appointmentRepository, times(1)).findById(1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testDeleteAppointment_Success() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(appointmentRepository).deleteById(1L);

        assertDoesNotThrow(() -> appointmentService.deleteAppointment(1L));

        verify(appointmentRepository, times(1)).existsById(1L);
        verify(appointmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAppointment_NotFound() {
        when(appointmentRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.deleteAppointment(1L);
        });

        verify(appointmentRepository, times(1)).existsById(1L);
        verify(appointmentRepository, never()).deleteById(1L);
    }
}
