package com.hackathon.domain.push.repository;

import com.hackathon.domain.push.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

	Optional<PushSubscription> findByEndpoint(String endpoint);

	Optional<PushSubscription> findByMember_IdAndEndpoint(Long memberId, String endpoint);

	List<PushSubscription> findAllByMember_Id(Long memberId);
}
