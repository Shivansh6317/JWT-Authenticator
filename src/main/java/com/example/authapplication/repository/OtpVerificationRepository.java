package com.example.authapplication.repository;

import com.example.authapplication.model.OtpVerification;
import com.example.authapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByUser(User user);
}

