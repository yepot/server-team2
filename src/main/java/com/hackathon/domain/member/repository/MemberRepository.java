package com.hackathon.domain.member.repository;

import com.hackathon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByUsername(String username);
	boolean existsByUsername(String username);
}
