package com.school.attendance.device.repository;

import com.school.attendance.device.entity.RegisteredDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, UUID> {
    Optional<RegisteredDevice> findByCredentialReference(String credentialReference);
}
