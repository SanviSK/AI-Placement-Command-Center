package com.placement.commandcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dsa_problems")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DsaProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(name = "leetcode_url", nullable = false)
    private String leetcodeUrl;

    private String tags; // Comma-separated tags
}
