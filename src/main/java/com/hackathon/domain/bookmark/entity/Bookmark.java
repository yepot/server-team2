package com.hackathon.domain.bookmark.entity;

import com.hackathon.domain.member.entity.Member;
import com.hackathon.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmark")
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false)
    private Integer viewCount;

    private LocalDateTime visitedAt;

    private LocalDateTime remindAt;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BookmarkTag> tags = new ArrayList<>();

    @Builder
    public Bookmark(Member memberId, String url, String title, String status, LocalDateTime remindAt) {
        this.memberId = memberId;
        this.url = url;
        this.title = title;
        this.remindAt = remindAt;
        this.viewCount = 0;
        this.isActive = true;
    }

    public List<BookmarkTag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void update(String url, String title, String status, LocalDateTime remindAt) {
        if (url != null) {
            this.url = url;
        }
        if (title != null) {
            this.title = title;
        }
        if (remindAt != null) {
            this.remindAt = remindAt;
        }
    }

    public void replaceTags(List<String> tagNames) {
        this.tags.clear();
        tagNames.forEach(this::addTag);
    }

    public void addTag(String name) {
        this.tags.add(new BookmarkTag(this, name));
    }

    public void delete() {
        this.isActive = false;
    }

    public void visit() {
        this.viewCount += 1;
        this.visitedAt = LocalDateTime.now();
    }
}

