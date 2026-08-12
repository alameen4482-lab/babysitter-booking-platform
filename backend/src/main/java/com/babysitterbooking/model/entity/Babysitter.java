package com.babysitterbooking.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "babysitters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Babysitter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String experience;

    @Column(nullable = false)
    private String skills;

    @Column(nullable = false)
    private Double hourlyRate;

    @Column(nullable = false)
    private Boolean available;
}