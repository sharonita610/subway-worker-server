package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "mail")
@NoArgsConstructor
public class MailEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String sender = "system";

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isRead = false;

    private boolean isClaimed = false;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
        }
    }

    public void markAsClaimed() {
        if (this.isClaimed) {
            throw new IllegalStateException("이미 수령된 보상입니다.");
        }
        this.isClaimed = true;
        this.isRead = true;
    }

}